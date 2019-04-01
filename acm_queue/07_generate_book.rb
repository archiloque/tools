#!/usr/bin/env ruby

require 'nokogiri'
require 'json'
require 'fileutils'
require 'addressable'

require_relative 'constants'

def create_if_not_exist(dir_path)
  unless File.exist? dir_path
    Dir.mkdir dir_path
  end
end

create_if_not_exist(BOOK_DIR)

IMAGES_TO_PROCESS = {}

def find_h_level(content)
  1.upto(3) do |level|
    possible_h = content.at("h#{level}")
    if possible_h
      if (possible_h.children[0].name == 'a') && (possible_h.children[0]['href'].start_with? '/listing.cfm')
        possible_h.remove
        possible_h = content.at("h#{level}")
      end
      if possible_h
        return level
      end
    end
  end
  nil
end

class ArticleContext

  attr_reader :h_level

  def initialize(document, h_level, images, index_issue, index_article, article_source_dir, article_url)
    @document = document
    @h_level = h_level
    @images = images
    @index_issue = index_issue
    @index_article = index_article
    @article_source_dir = article_source_dir
    @article_url = article_url
  end

  def process_image(element, article_image_url)
    image_full_url = full_link(element, article_image_url)
    image_file_name = @images[image_full_url]


    image_target_file = File.join(@index_issue.to_s, @index_article.to_s, image_file_name)
    target_file = File.join(BOOK_DIR, image_target_file)
    source_file = File.join(@article_source_dir, image_file_name)
    IMAGES_TO_PROCESS[source_file] = target_file
    image_target_file
  end

  def full_link(element, relative_link)
    if relative_link.nil?
      p element
      raise
    end
    Addressable::URI.join(@article_url, relative_link).to_s
  end

  def current_article
    "#{@index_issue}-#{@index_article}"
  end

  def replace_node(node, text)
    text_node = Nokogiri::XML::Text.new(text, @document)
    node.replace(text_node)
  end

end

def clean_before(node)
  while (node.previous) && (node.previous.text?) && (node.previous.text.strip.length == 0)
    node.previous.remove
  end
end

def clean_after(node)
  while (node.next) && (node.next.text?) && (node.next.text.strip.length == 0)
    node.next.remove
  end
end

def process_node(node, article_context)
  node.children.each do |c|
    process_node(c, article_context)
  end
  if node.text?
  elsif node.element?
    case node.name
    when 'h1', 'h2', 'h3', 'h4', 'h5', 'h6'
      current_h = node.name[1..-1].to_i
      text = "\n\n==#{'=' * (current_h - article_context.h_level)} #{node.text.strip}\n\n"
      article_context.replace_node(node, text)
    when 'i', 'em'
      text = "__#{node.text.strip}__"
      article_context.replace_node(node, text)
    when 'b', 'strong'
      text = "**#{node.text.strip}**"
      article_context.replace_node(node, text)
    when 'sup'
      text = "^#{node.text}^"
      article_context.replace_node(node, text)
    when 'sub'
      text = "~#{node.text}~"
      article_context.replace_node(node, text)
    when 'p'
      text = "\n\n#{node.text.strip}\n\n"
      article_context.replace_node(node, text)
    when 'a'
      if node['name']
        text = "[[#{article_context.current_article}-#{node['name']}]]"
        article_context.replace_node(node, text)
      else
        text = "link:#{article_context.full_link(node, node['href'])}[#{node.text}]"
        article_context.replace_node(node, text)
      end
    when 'img'
      text = "\n\nimage::#{article_context.process_image(node, node['src'])}[]\n\n"
      article_context.replace_node(node, text)
    when 'br'
      text = " +\n"
      article_context.replace_node(node, text)
    when 'blockquote'
      text = "\n\n[quote]\n____\n#{node.text.strip}\n____\n"
      article_context.replace_node(node, text)
    when 'li'
      case node.parent.name
      when 'ol'
        prefix = '*'
      when 'ul'
        prefix = '.'
      else
        p node
        raise
      end
      text = "\n#{prefix} #{node.text.strip}\n"
      article_context.replace_node(node, text)
    when 'ol', 'ul'
      text = "\n\n#{node.text}\n"
      article_context.replace_node(node, text)
    when 'code', 'tt'
      text = "`#{node.text}`"
      article_context.replace_node(node, text)
    when 'pre'
      text = "\n\n....\n#{node.text}\n....\n\n"
      article_context.replace_node(node, text)
    when 'hr'
      text = "\n\n'''\n\n"
      article_context.replace_node(node, text)
    when 'u'
      text = "##{node.text}#"
      article_context.replace_node(node, text)
    when 'td', 'th'
      clean_before(node)
      clean_after(node)
      text = "|#{node.text.strip}"
      article_context.replace_node(node, text)
    when 'tr'
      clean_before(node)
      clean_after(node)
      text = "#{node.text.strip}\n"
      article_context.replace_node(node, text)
    when 'table'
      text = "\n\n|===\n#{node.text}|===\n\n"
      article_context.replace_node(node, text)
    when 'iframe'
      text = "\n\nlink:#{node['src']}[iframe]\n\n"
      article_context.replace_node(node, text)
    when 'font', 'nobr', 'wbr', 's', 'tbody'
      text = node.text
      article_context.replace_node(node, text)
    when 'strike'
      text = "[line-through]##{node.text}#"
      article_context.replace_node(node, text)
    when 'span'
      if ['ldq'].include? node['class']
        node.remove
      elsif ['Qbioname', '__cf_email__', 'apple-converted-space', 'Qtablebodycopy', 'MsoHyperlink', 'MsoFootnoteReference', 'style1', 'body'].include? node['class']
        text = node.text
        article_context.replace_node(node, text)
      elsif node['class'] == 'Qcodingsingleword'
        text = "`#{node.text}`"
        article_context.replace_node(node, text)
      elsif node['class'].nil?
        text = node.text
        article_context.replace_node(node, text)
      else
        p node
        raise
      end
    when 'div'
      if node['class'].nil?
        text = "\n\n#{node.text}\n\n"
        article_context.replace_node(node, text)
      elsif ['MsoNormal'].include? node['class']
        text = "\n\n#{node.text}\n\n"
        article_context.replace_node(node, text)
      elsif ['container'].include? node['class']

      else
        p node
        raise
      end
    else
      p node
      raise
    end
  else
    p node
    raise
  end
end

def process_content(content, images, index_issue, index_article, article_source_dir, article_url)
  h_level = find_h_level(content)
  unless h_level
    raise
  end

  first_h = content.at("h#{h_level}")
  while first_h.previous
    first_h.previous.remove
  end
  first_h.remove

  stamp = content.xpath("//img[@src='img/q stamp_small.jpg']")[0].parent
  while stamp.next
    stamp.next.remove
  end
  stamp.remove

  article_context = ArticleContext.new(content, h_level, images, index_issue, index_article, article_source_dir, article_url)

  result = ''
  container = content.at('.container')
  process_node(container, article_context)
  result << container.text
  result
end

if File.exist? 'logo.gif'
  FileUtils.copy_file 'logo.gif', File.join(BOOK_DIR, 'logo.gif')
end

chapters = []

LINE_START_WITH_NUMBER = /\A(\d+).(.*)\z/
LINE_START_WITH_CHAR = /\A([a-zA-Z]{1})\.(.*)\z/

time = Time.new
version_string = "v1.0, #{time.year}-#{time.month}-#{time.day}\n"

issues = JSON.parse(IO.read(ISSUES_WITH_ARTICLES_JSON_FILE))
issues.each_with_index do |issue, index_issue|
  # p "Processing issue #{index_issue} [#{issue['href']}]"
  issue_target_dir = File.join(BOOK_DIR, index_issue.to_s)
  create_if_not_exist(issue_target_dir)

  issues_articles = []
  issue['articles'].each_with_index do |article, index_article|
    if (index_issue == 83 && index_article == 3) ||
        (index_issue == 104 && index_article == 8) ||
        (index_issue == 106 && index_article == 9) ||
        (index_issue == 106 && index_article == 10) ||
        (index_issue == 108 && index_article == 10) ||
        (index_issue == 108 && index_article == 11) ||
        (index_issue == 110 && index_article == 0) ||
        (index_issue == 111 && index_article == 0) ||
        (index_issue == 110 && index_article == 2) ||
        (index_issue == 111 && index_article == 2) ||
        (index_issue == 111 && index_article == 8) ||
        (index_issue == 114 && index_article == 6) ||
        (index_issue == 115 && index_article == 8) ||
        (index_issue == 117 && index_article == 7) ||
        (index_issue == 117 && index_article == 8) ||
        (index_issue == 118 && index_article == 1)
      next
    end
    article_source_dir = File.join(ISSUES_DIR, index_issue.to_s, index_article.to_s)
    article_source_file = File.join(article_source_dir, 'index.html')
    if File.exist?(article_source_file)
      article_target_dir = File.join(issue_target_dir, index_article.to_s)
      create_if_not_exist(article_target_dir)

      chapter_name = "#{index_issue}-#{index_article}"
      target_article_file = File.join(BOOK_DIR, index_issue.to_s, index_article.to_s, 'index.adoc')

      # p "Processing article #{index_issue}-#{index_article} [#{article_source_file}] to [#{target_article_file}]"
      doc = File.open(article_source_file) {|f| Nokogiri::HTML(f)}
      article_source_image_file = File.join(article_source_dir, 'images.json')
      if File.exist?(article_source_image_file)
        images_infos = JSON.parse(IO.read(article_source_image_file))
      else
        images_infos = {}
      end
      title = doc.at_css('title').text[0..-13]
      article_content = (process_content(doc, images_infos, index_issue, index_article, article_source_dir, article['href'])).to_s.split("\n")
      article_content.each_with_index do |line, index|
        match = LINE_START_WITH_NUMBER.match(line)
        if match
          line = "+#{match[1]}+.#{match[2]}"
          article_content[index] = line
        else
          match = LINE_START_WITH_CHAR.match(line)
          if match
            line = "+#{match[1]}+.#{match[2]}"
            article_content[index] = line
          end
        end
        if line.end_with?(' +')
          if line[0..-2].strip.length == 0
            article_content[index] = ''
          elsif index < (article_content.length - 1)
            article_content[index + 1].lstrip!
          end
        end
      end
      File.open(target_article_file, 'w') do |out|
        out << "[##{chapter_name}]\n"
        out << "== #{title}\n"
        out << ":author: ACM Queue\n"
        out << ":lang: en\n"
        out << ":figure-caption!:\n"
        out << "\n"
        out << article_content.join("\n")
      end
      issues_articles << File.join(index_issue.to_s, index_article.to_s, 'index.adoc')
    end
  end

  if issues_articles.empty?
    p "Issue is empty, skipping it !"
  else
    File.open(File.join(BOOK_DIR, "#{index_issue}.adoc"), 'w') do |out|
      out << "[##{index_issue}]\n"
      out << "= #{Nokogiri::HTML::fragment(issue['name']).inner_text.strip.gsub(/\s+/, ' ')}\n"
      out << ":author: ACM Queue\n"
      out << ":lang: en\n"
      out << ":figure-caption!:\n"
      out << "\n"
      issues_articles.each do |article|
        out << "include::#{article}[]\n"
      end
    end
    chapters << index_issue
  end

end

require 'fileutils'
IMAGES_TO_PROCESS.each_pair do |key, value|
  FileUtils.copy(key, value)
end

File.open(File.join(BOOK_DIR, 'index.adoc'), 'w') do |out|
  out << "= ACM Queue\n"
  out << version_string
  out << ":doctype: book\n"
  out << ":lang: en\n"
  out << ":figure-caption!:\n"
  if File.exist? 'logo.gif'
    out << ":front-cover-image: image:gif.png[width=160,height=49]\n"
  end
  out << "\n"
  chapters.reverse.each do |chapter|
    out << "include::#{chapter}.adoc[]\n"
  end
end

p "Converting"
`asciidoctor -a data-uri -D output data/book/index.adoc`
`asciidoctor-epub3 -D output data/book/index.adoc`
