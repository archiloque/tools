require 'rubygems'
require 'nokogiri'
require 'httpclient'
require 'lib/models'
require 'addressable/uri'

client = HTTPClient.new

if Article.count == 0
  Article.create(:url => 'http://www.lefigaro.fr/international/2011/01/06/01003-20110106ARTFIG00716-les-egyptiens-ne-sont-pas-des-extremistes.php')
end

found_something = true
while found_something
  Article.filter(:fetched => false).each do |article|
    DB.transaction do
      p article.url
      article.fetched = true
      doc             = Nokogiri::HTML(client.get(Addressable::URI.parse(article.url).normalize.to_str).content)
      images          = doc.search('//link[@rel="image_src"]')
      if images.length > 0
        article.image_url = images[0]['href']
        File.open(article.image_path, 'w') { |f| f.write(client.get(article.image_url).content) }
      end
      article.save
      doc.search('//span[@class="picto picto-mots-cles"]/a').each do |t|
        t_url = t['href']
        t_url = t_url[(t_url.rindex("/") + 1) .. -1]
        tag   = Tag.filter(:url_fragment => t_url).first
        unless tag
          tag = Tag.create(:url_fragment => t_url, :text => t.text)
        end
        article.add_tag(tag)
      end
    end
  end
  found_something = false
  Tag.filter(:fetched => false).each do |tag|
    p tag.text
    DB.transaction do
      tag.fetched = true
      doc         = Nokogiri::HTML(client.get("http://plus.lefigaro.fr/tag/#{tag.url_fragment}").content)
      doc.search('//h2').each do |h2|
        possible_urls = h2.search('a')
        if possible_urls.length > 0
          url = possible_urls[0]['href']
          if (Addressable::URI.parse(url).host == 'www.lefigaro.fr') && (!Article.filter(:url => url).first)
            Article.create(:url => url)
            found_something = true
          end
        end
      end
      doc.search('//li[@class="pager-last last"]/a').each do |last|
        last = URI.parse(last['href']).query
        last = last[(last.index('=') + 1) .. -1].to_i
        2.upto(last) do |i|
          p "#{tag.text} #{i}"
          doc = Nokogiri::HTML(client.get("http://plus.lefigaro.fr/tag/#{tag.url_fragment}?page=#{i}").content)
          doc.search('//h2').each do |h2|
            possible_links = h2.search('a')
            if possible_links.length > 0
              url = possible_links[0]['href']
              if (Addressable::URI.parse(url).host == 'www.lefigaro.fr') && (!Article.filter(:url => url).first)
                Article.create(:url => url)
                found_something = true
              end
            end
          end
        end
      end
      tag.save
    end
  end
end