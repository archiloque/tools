#!/usr/bin/env ruby

require 'addressable'
require 'curb'
require 'json'
require 'nokogiri'
require_relative 'constants'

issues = JSON.parse(IO.read(ISSUES_JSON_FILE))
articles = {}
issues.each_pair do |issue_name, issue_url|
  p issue_url
  issues_articles = []
  http = Curl.get(issue_url)
  document = Nokogiri::HTML(http.body_str)

  document.css('#toc a').each do |link|
    if link['title'] != 'View Abstract'
      text = link.text.gsub("\n",' ').gsub(/\s+/, ' ')
      issues_articles << {
        title: text,
        url: Addressable::URI.join(issue_url, link['href'])
      }
    end
  end

  articles[issue_name] = {
    url: issue_url,
    articles: issues_articles,
  }
end

File.open(ARTICLES_JSON_FILE, 'w') do |f|
  f.write(JSON.pretty_generate(articles))
end
