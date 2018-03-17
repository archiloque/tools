#!/usr/bin/env ruby

require 'addressable'
require 'curb'
require 'json'
require 'nokogiri'
require_relative 'constants'

unless File.exist? ARTICLES_DIR
  Dir.mkdir ARTICLES_DIR
end

issues = JSON.parse(IO.read(ARTICLES_JSON_FILE))
index = 0
issues.each_pair do |issue_name, issue_content|
  issue_content['articles'].each do |article|
    if article['title'] != 'Author Biographies'
      article_url = article['url']
      p article_url
      parsed_article_url = article_url.match(/\A(?<address>http:\/\/digitalhumanities.org\/dhq\/vol\/.+).html\z/)
      if parsed_article_url
        # Not a bio
        adress_part = parsed_article_url[:address]
        article_xml_url = "#{adress_part}.xml"
        http = Curl.get(article_xml_url)
        open(File.join(ARTICLES_DIR, "#{index}.xml"), 'w') do |f|
          f.puts http.body_str
        end
        article['index'] = index
        index += 1
      end
    end
  end
end

File.open(ARTICLES_WITH_ID_JSON_FILE, 'w') do |f|
  f.write(JSON.pretty_generate(issues))
end
