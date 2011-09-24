require_relative '../lib/models'

require 'nokogiri'
require 'em-http'
require 'addressable/uri'

@@running = 0
@@waiting = []

def add_article params
  article_uri = params[0]
  article_id = params[1]
  http = EventMachine::HttpRequest.new(Addressable::URI.parse(article_uri).normalize.to_str, :redirects => 1).get
  http.callback do
    if http.response_header.status == 200
      doc = Nokogiri::HTML(http.response)
      article = Article[article_id]
      if (image = doc.at('//link[@rel="image_src"]')) && (image['href'] != "")
        article.image_url = image['href']
        register_task :image, [article.image_url, article.image_path]
        article.save
      end
      doc.search('//span[@class="picto picto-mots-cles"]/a').each do |t|
        t_url = t['href']
        t_url = t_url[(t_url.rindex("/") + 1) .. -1]
        tag = Tag.filter(:url_fragment => t_url).first
        unless tag
          tag = Tag.create(:url_fragment => t_url, :text => t.text)
          register_task :tag, [t_url]
        end
        article.add_tag(tag)
      end
    end
    end_task
  end
  http.errback do
    end_task
  end
end

def add_image params
  image_url = params[0]
  image_path = params[1]
  http = EventMachine::HttpRequest.new(image_url).get
  http.callback do
    File.open(image_path, 'w') { |f| f.write(http.response) }
    end_task
  end
  http.errback do
    end_task
  end
end

def add_tag params
  url_fragment = params[0]
  http = EventMachine::HttpRequest.new("http://plus.lefigaro.fr/tag/#{url_fragment}", :redirects => 1).get
  http.callback do
    if http.response_header.status == 200
      doc = Nokogiri::HTML(http.response)
      doc.search('//h2').each do |h2|
        if possible_url = h2.at('a')
          url = possible_url['href']
          if (Addressable::URI.parse(url).host == 'www.lefigaro.fr') && (!Article.filter(:url => url).first)
            article = Article.create(:url => url)
            register_task :article, [url, article.id]
          end
        end
      end
      doc.search('//li[@class="pager-last last"]/a').each do |last|
        last = URI.parse(last['href']).query
        last = last[(last.index('=') + 1) .. -1].to_i
        2.upto(last) do |i|
          register_task :sub_tag, sub_tag_url(url_fragment, i)
        end
      end
    end
    end_task
  end
  http.errback do
    end_task
  end
end

def sub_tag_url url_fragment, page
  "http://plus.lefigaro.fr/tag/#{url_fragment}?page=#{page}"
end

def add_sub_tag params
  tag_url = params[0]
  http = EventMachine::HttpRequest.new("http://plus.lefigaro.fr/tag/#{tag_url}").get
  http.callback do
    if http.response_header.status == 200
      doc = Nokogiri::HTML(http.response)
      doc.search('//h2').each do |h2|
        possible_links = h2.search('a')
        if possible_links.length > 0
          url = possible_links[0]['href']
          if (Addressable::URI.parse(url).host == 'www.lefigaro.fr') && (!Article.filter(:url => url).first)
            register_task :article, Article.create(:url => url)
          end
        end
      end
    end
    end_task
  end
  http.errback do
    end_task
  end
end

def end_task
  @@running -= 1
  if @@running < 10
    next_element = @@waiting.pop
    if next_element
      start_task next_element[0], next_element[1]
    elsif @@running <= 0
      EM.stop
    end
  end
end

def register_task type, params
  if @@running < 10
    start_task type, params
  else
    p "Register #{type} #{params}, #{@@waiting.length}"
    @@waiting << [type, params]
  end
end

def start_task type, params
  @@running += 1
  p "Start #{type} #{params}"
  if type == :article
    add_article params
  elsif type == :image
    add_image params
  elsif type == :sub_tag
    add_sub_tag params
  else
    add_tag params
  end
end

EM.run do
  if Article.count == 0
    article = Article.create(:url => 'http://www.lefigaro.fr/conjoncture/2011/09/19/04016-20110919ARTFIG00468-taxer-les-plus-fortunes-ameliorerait-le-bien-etre.php')
    register_task :article, [article.url, article.id]
  end

  if @@running <= 0
    EM.stop
  end
end