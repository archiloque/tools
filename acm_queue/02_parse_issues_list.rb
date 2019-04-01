#!/usr/bin/env ruby

require 'nokogiri'
require 'json'
require 'addressable'

require_relative 'constants'

doc = File.open(MAIN_HTML_FILE) { |f| Nokogiri::HTML(f) }
issues = []

doc.css('#primary .image-list').each do |issue|
  issues << {
      'href': Addressable::URI.join(MAIN_URI, issue.at('a')['href']),
      'name': issue.at('h2').text
  }
end

File.open(ISSUES_JSON_FILE, 'w') do |f|
  f.write(JSON.pretty_generate(issues))
end

