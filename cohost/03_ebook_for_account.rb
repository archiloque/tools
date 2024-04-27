#!/usr/bin/env ruby

require "set"
require "json"
require "nokogiri"
require "kramdown-asciidoc"
require "time"

require_relative 'shared'

raise "2 parameters: the blog name and optionaly the tag to filter" unless [1, 2].include? ARGV.length

blog_name = ARGV[0]
tag = (ARGV.length > 1) ? ARGV[1] : nil
posts = JSON.parse(IO.read(json_file(blog_name)))
if tag
  filtered_posts = posts.filter { |post| post["tags"].include?(tag) }
else
  filtered_posts = posts
end
p "#{filtered_posts.length} posts found"

all_ids = Set.new(filtered_posts.map{|post| post["postId"].to_s})

posts_ids = Set.new

filtered_posts.each do |post|
  post_file_name = post["filename"]
  post_id = post["postId"]
  if ! posts_ids.include?(post_id)
    p "Processing #{post_id}"
    content = []
    if post["headline"] && (post["headline"].length > 0)
      content << "# #{post["headline"]}"
    end

    post["blocks"].each do |block|
      if block["type"] == "markdown"
        content << block["markdown"]['content']
      else
        pp block
      end
    end

    unless content.empty?
      asciidoc = Kramdoc.convert(content.join("\n\n"))
      asciidoc = asciidoc.gsub(/https?:\/\/cohost.org\/#{blog_name}\/post\/(\d+)-([a-z\-\d]+)\[/) do |file_name|
        if all_ids.include?($1)
          "xref:#{$1}["
        else
          "link:https://cohost.org/#{blog_name}/post/#{$1}-#{$2}["
        end
      end

      date = Time.iso8601(post['publishedAt']).strftime('%Y/%m/%d')
      asciidoc << "\n#{post['tags'].map{|t| "##{t}"}.join(", ")} link:https://cohost.org/#{blog_name}/post/#{post_file_name}[#{date}] \n"

      target_post_file = File.join(asciidoctor_directory(blog_name, tag || "all"), "#{post_id}.adoc")    
      File.open(target_post_file, "w") do |f| 
        pp "Creating [#{target_post_file}]"
        f.write("[##{post_id}]\n")
        f.write(asciidoc)
      end
      posts_ids << post_id
    end
  end
end

target_main_file = File.join(asciidoctor_directory(blog_name, tag || "all"), "main.adoc")

File.open(target_main_file, "w") do |f|
  f << "= #{blog_name}\n"
  f << ":doctype: book\n"
  f << "\n"
  posts_ids.each do |post_id|
    f << "include::#{post_id}.adoc[]\n\n'''\n\n"
  end
end