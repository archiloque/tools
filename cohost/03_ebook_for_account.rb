#!/usr/bin/env ruby

require "set"
require "json"
require "nokogiri"
require "kramdown-asciidoc"

require_relative 'shared'

raise "2 parameters: the blog name and the tag to filter" if ARGV.length != 2

blog_name = ARGV[0]
tag = ARGV[1]
posts = JSON.parse(IO.read(json_file(blog_name)))
filtered_posts = posts.filter { |post| post["tags"].include?(tag) }
p "#{filtered_posts.length} posts found"

all_ids = Set.new(filtered_posts.map{|post| post["postId"].to_s})

posts_ids = Set.new

filtered_posts.each do |post|
  post_file_name = post["filename"]
  post_id = post["postId"]
  if ! posts_ids.include?(post_id)
    p "Processing #{post_id}"
    content = []
    post["blocks"].each do |block|
      if block["type"] == "markdown"
        content << block["markdown"]['content']
      else
        pp block
      end
      asciidoc = Kramdoc.convert(content.join("\n\n"))
      asciidoc = asciidoc.gsub(/https?:\/\/cohost.org\/#{blog_name}\/post\/(\d+)-([a-z\-\d]+)\[/) do |file_name|
        if all_ids.include?($1)
          "xref:#{$1}["
        else
          "link:https://cohost.org/#{blog_name}/post/#{$1}-#{$2}["
        end
      end

      asciidoc << "\n#{post['tags'].map{|t| "##{t}"}.join(", ")} link:https://cohost.org/#{blog_name}/post/#{post_file_name}[#{post['publishedAt']}] \n"

      target_post_file = File.join(asciidoctor_directory(blog_name, tag), "#{post_id}.adoc")    
      File.open(target_post_file, "w") do |f| 
        f.write("[##{post_id}]\n")
        f.write(asciidoc)
      end
      posts_ids << post_id
    end
  end
end

target_main_file = File.join(asciidoctor_directory(blog_name, tag), "main.adoc")

File.open(target_main_file, "w") do |f|
  f << "= #{blog_name}\n"
  f << ":doctype: book\n"
  f << "\n"
  posts_ids.each do |post_id|
    f << "include::#{post_id}.adoc[]\n\n'''\n\n"
  end
end