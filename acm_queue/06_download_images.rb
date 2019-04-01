#!/usr/bin/env ruby

require 'curb'
require 'json'
require 'nokogiri'
require 'addressable'

require_relative 'constants'

issues = JSON.parse(IO.read(ISSUES_WITH_ARTICLES_JSON_FILE))

def find_h(doc, value)
  h = doc.at("h#{value}")
  if h
    previous_h = h.previous
    while previous_h
      previous_h.remove
      previous_h = h.previous
    end
    true
  else
    false
  end
end

def find_image_file_name(images, image_url)
  current_image_index = 0
  image_extension = Addressable::URI.parse(image_url).extname
  while true
    image_file_name = "#{current_image_index}#{image_extension}"
    unless images.has_value?(image_file_name)
      return image_file_name
    end
    current_image_index += 1
  end
end

def write_article_images_infos(article_images_infos, images)
  File.open(article_images_infos, 'w') do |f|
    f.write(JSON.pretty_generate(images))
  end
end
def process_article(article, article_images_infos, index_article, index_issue, issue, source_article_file, article_dir)
  p "Processing article #{index_issue}-#{index_article} [#{article['href']}] [#{source_article_file}]"

  if File.exist?(article_images_infos)
    images = JSON.parse(IO.read(article_images_infos))
  else
    images = {}
  end

  doc = File.open(source_article_file) {|f| Nokogiri::HTML(f)}

  if doc.at('h3:contains("Item not available")')
    p "Item not available"
    return
  end

  unless find_h(doc, 1)
    unless find_h(doc, 2)
      unless find_h(doc, 3)
        p "!!! Skipping !!!"
        return
      end
    end
  end

  stamp = doc.xpath("//img[@src='img/q stamp_small.jpg']")[0].parent
  while stamp.next
    stamp.next.remove
  end
  stamp.remove

  doc.css('.container').css('img').each do |image|
    image_url = Addressable::URI.join(issue['href'], image['src']).to_s

    if images.key?(image_url)
      image_file = File.join(article_dir, images[image_url])
      if File.exist?(image_file)
        if File.size(image_file) == 0
          p "Deleting [#{image_url}] at [#{image_file}]"
          File.unlink(image_file)
          images.delete(image_url)
        else
          p "Image [#{image_url}] exists at [#{image_file}]"
        end
      else
        p "Missing [#{image_url}] at [#{image_file}]"
        images.delete(image_url)
      end
    end

    unless images.key?(image_url)
      image_file_name = find_image_file_name(images, image_url)
      p "Downloading #{index_issue}-#{index_article} [#{image_url}] at [#{image_file_name}]"
      image_url_https = Addressable::URI.parse(image_url)
      image_url_https.scheme = 'https'
      image_url_https_s = image_url_https.to_s
      Curl::Easy.download(image_url_https_s, File.join(article_dir, image_file_name))
      images[image_url] = image_file_name
      write_article_images_infos(article_images_infos, images)
      sleep(2)
    end
  end
end

issues.each_with_index do |issue, index_issue|
  p "Processing issue #{index_issue} [#{issue['href']}]"
  issue['articles'].each_with_index do |article, index_article|
    article_dir = File.join(ISSUES_DIR, index_issue.to_s, index_article.to_s)
    source_article_file = File.join(article_dir, 'index.html')
    article_images_infos = File.join(article_dir, 'images.json')
    process_article(article, article_images_infos, index_article, index_issue, issue, source_article_file, article_dir)
  end
end
