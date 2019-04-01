#!/usr/bin/env ruby

require 'curb'
require 'json'

require_relative 'constants'

issues = JSON.parse(IO.read(ISSUES_WITH_ARTICLES_JSON_FILE))
issues.each_with_index do |issue, index_issue|
  p "Processing issue #{index_issue}"
  issue['articles'].each_with_index do |article, index_article|
    article_dir = File.join(ISSUES_DIR, index_issue.to_s, index_article.to_s)
    unless File.exist? article_dir
      Dir.mkdir article_dir
    end
    target_file = File.join(article_dir, 'index.html')
    unless File.exist? target_file
      p "Processing article #{index_issue}-#{index_article} [#{article['href']}] to [#{target_file}]"
      Curl::Easy.download(article['href'], target_file)
      sleep(2)
    end
  end
end
