#!/usr/bin/env ruby

require "curb"
require "json"
require "nokogiri"
require "addressable"

require_relative "shared"

raise "1 parameter: the blog name" if ARGV.length != 1

def download(blog_name:, page:)
  STDOUT << "Processing #{blog_name} #{page}\n"
  file_path = File.join(page_directory(blog_name), "#{page}.html")
  if File.exist?(file_path)
    content = IO.read(file_path)
  else
    download_url =
      Addressable::URI.encode("https://cohost.org/#{blog_name}/?page=#{page}")
    STDOUT << "Downloading [#{download_url}]\n"
    sleep(2)
    content = Curl.get(download_url).body
    File.open(file_path, "w") { |f| f.write(content) }
  end
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
    return data["posts"].length > 0 if data && data["posts"]
  end
  return false
end

blog_name = ARGV[0]

index = 0
found = true
while found
  found = download(blog_name: blog_name, page: index)
  index += 1
end
