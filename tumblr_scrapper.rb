require 'rubygems'
require 'open-uri'
require 'nokogiri'
require 'addressable/uri'
require 'typhoeus'

if ARGV.length != 1
  raise "Need the tumblr name"
end

tumblr_name = ARGV[0]

DIR_NAME = ".tumblr_scrapper_#{tumblr_name}"

if File.exist? DIR_NAME
  FileUtils.rm_rf DIR_NAME
end

Dir.mkdir DIR_NAME

base_url = "http://#{tumblr_name}.tumblr.com/api/read?filter=html&num=50&type=photo"


def add_photo url, index, hydra
  request = Typhoeus::Request.new url
  request.timeout = 60000
  request.connect_timeout = 6000
  request.on_complete do |response|
    if response.code == 302
      add_photo(response.headers_hash['Location'], index, hydra)
    else
      p "#{index} #{response.code} #{url}"
      File.open("#{DIR_NAME}/#{index}#{File.extname(URI.parse(url).path)}", 'w') { |f| f.write(response.body) }
    end
  end
  hydra.queue request
end

@@hydras_threads = []

p "Starting to list"
index = 0
new_posts = true
while new_posts
  new_posts = false
  begin
    doc = Nokogiri::HTML(open("#{base_url}&start=#{index}"))
  rescue
    doc = Nokogiri::HTML(open("#{base_url}&start=#{index}"))
  end

  hydra = Typhoeus::Hydra.new({:max_concurrency => 5})
  hydra.disable_memoization

  doc.search('post').each do |post|
    new_posts = true
    index += 1
    add_photo post.search('photo-url')[0].content, index, hydra
  end
  @@hydras_threads << Thread.new do
    p "Starting thread #{Thread.current}"
    hydra.run
    @@hydras_threads.delete(Thread.current)
    p "Ending thread #{Thread.current}"
  end
end

p "End listing, wait for scrapping end"

until @@hydras_threads.empty?
  p "Joining thread #{@@hydras_threads.first}"
  @@hydras_threads.first.join
end
p "Done"
