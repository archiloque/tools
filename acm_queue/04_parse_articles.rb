#!/usr/bin/env ruby

require 'nokogiri'
require 'json'
require 'addressable'

require_relative 'constants'

issues = JSON.parse(IO.read(ISSUES_JSON_FILE))
issues.each_with_index do |issue, index|
  issue_file = File.join(ISSUES_DIR, index.to_s, 'index.html')
  p "Reading #{index} [#{issue_file}]"
  doc = File.open(issue_file) {|f| Nokogiri::HTML(f)}
  issue['articles'] = doc.css('#primary h3').map do |article|
    inner_link = article.at('a')
    if inner_link
      {
          'href': Addressable::URI.join(issue['href'], inner_link['href']),
          'name': inner_link.text
      }
    else
      nil
    end
  end.compact
end

File.open(ISSUES_WITH_ARTICLES_JSON_FILE, 'w') do |f|
  f.write(JSON.pretty_generate(issues))
end
