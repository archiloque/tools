#!/usr/bin/env ruby

require "json"

require_relative "shared"

raise "1 parameter: the blog name" if ARGV.length != 1

blog_name = ARGV[0]
posts =  JSON.parse(IO.read(json_file(blog_name)))
posts_per_tags = {}
posts.each do |post|
  post_file_name = post["filename"]
  post["tags"].each do |tag|
    if posts_per_tags.key?(tag)
      posts_per_tags[tag] << post_file_name
    else
      posts_per_tags[tag] = [post_file_name]
    end
  end
end

File.open(File.join(data_directory(blog_name), "tags.txt"), "w") do |f|
  posts_per_tags.keys.sort.each do |tag|
    f << "[#{tag}] #{posts_per_tags[tag].sort.uniq.map { |post| "https://cohost.org/#{blog_name}/post/#{post}" }.join(" ")}\n"
  end
end
