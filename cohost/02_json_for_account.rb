#!/usr/bin/env ruby

require "json"
require "nokogiri"

require_relative 'shared'

raise "1 parameter: the blog name" if ARGV.length != 1

def load_page(blog_name:, page:, posts:)
  STDOUT << "Processing #{blog_name} #{page}\n"
  file_path = File.join(page_directory(blog_name), "#{page}.html")
  if File.exist?(file_path)
    content = IO.read(file_path)
    document = Nokogiri.HTML(content)
    json_data =
      JSON.parse(
        document
          .css("script")
          .select { |script| script["id"] == "trpc-dehydrated-state" }[
          0
        ].text
      )
    json_data["queries"].each do |query|
      data = query["state"]["data"]
      if data
        (data["posts"] || []).each do |post|
          posts << post
          post_id = post["postId"]
          File.open(
            File.join(json_directory(blog_name), "#{post_id}.json"),
            "w"
          ) { |f| f.write(JSON.pretty_generate(post)) }
        end
      end
      data["posts"].each { |post| posts << post } if data && data["posts"]
    end
    return true
  else
    return false
  end
end

blog_name = ARGV[0]
page = 0
found = true
posts = []

while found
  found = load_page(blog_name: blog_name, page: page, posts: posts)
  page += 1
end
p "#{posts.length} posts found"
File.open(json_file(blog_name), "w") { |f| f.write(posts.to_json) }
