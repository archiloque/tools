#!/usr/bin/env ruby

require 'addressable'
require 'curb'
require 'json'
require 'nokogiri'
require 'set'
require_relative 'constants'

[BOOK_DIR, FIGURES_DIR, CONVERTED_FIGURES_DIR].each do |dir|
  unless File.exist?(dir)
    Dir.mkdir dir
  end
end

unless File.exist? FIGURES_JSON_FILE
  IO.write(FIGURES_JSON_FILE, JSON.pretty_generate({}), mode: 'w')
end

FIGURES = JSON.parse(IO.read(FIGURES_JSON_FILE))

def no_break(text)
  text.tr("\n", ' ').gsub(/\s+/, ' ')
end

def clean_text(text)
  text.gsub(/(\n +)/, "\n").strip
end

def print_content_article(article_id, article_url, article_document, article_file)
  abstract_element = article_document.at_xpath('//dhq:abstract')
  unless abstract_element.blank?
    article_file << "[abstract]\n=== Abstract\n\n"
    article_file << process_node_to_text(article_id, article_url, abstract_element)
  end

  article_file << process_node_to_text(article_id, article_url, article_document.at_xpath('//xmlns:body'))

  if article_document.at_xpath('//xmlns:listBibl/xmlns:bibl')
    article_file << "=== Works Cited\n\n#{process_node_to_text(article_id, article_url, article_document.at_xpath('//xmlns:listBibl'))}"
  end
  article_file << "\n"
end

def process_node_to_text(article_id, article_url, node)
  node.children.each do |child|
    process_node(article_id, article_url, child)
  end
  node.text
end

def swap_cdata(node, text)
  node.swap(node.document.create_cdata(text))
end

def get_figure_id(article_url, figure_url)
  full_url = Addressable::URI.join(article_url, figure_url).to_s
  if FIGURES.key? full_url
    FIGURES[full_url]
  else
    figure_extension = Addressable::URI.parse(full_url).extname
    figure_id = "#{FIGURES.length}#{figure_extension}"
    FIGURES[full_url] = figure_id
    figure_id
  end
end

def get_link(article_id, ptr_node)
  link = ptr_node.attr('target')
  if link.start_with?('#')
    "<<#{link[1..-1]}-#{article_id}>>"
  else
    "link:#{link}[]"
  end
end

def image_path(image)
  File.absolute_path("data/converted/#{image}")
end

# @param [Nokogiri::XML::Element] node
def process_node(article_id, article_url, node)
  if node.name == 'cit'
    if (node.xpath('./xmlns:quote').length == 1) && (node.xpath('./xmlns:quote')[0].attr('rend') == 'block') &&
        (node.xpath('./xmlns:ptr').length >= 1)
      prts_texts = []
      node.xpath('./xmlns:ptr').each do |ptr_node|
        prts_texts << get_link(article_id, ptr_node)
        ptr_node.unlink
      end
      quote_node = node.at_xpath('./xmlns:quote')
      swap_cdata(node, "\n\n[quote, #{prts_texts.join(' ')}]\n____\n#{process_node_to_text(article_id, article_url, quote_node)}\n____\n\n")
      return
    elsif (node.xpath('./xmlns:quote').length == 1) && (node.xpath('./xmlns:quote')[0].attr('rend') == 'block') &&
        (node.xpath('./dhq:citRef').length == 1)
      link_content = process_node_to_text(article_id, article_url, node.at_xpath('./dhq:citRef'))
      quote_node = node.at_xpath('./xmlns:quote')
      swap_cdata(node, "\n\n[quote, #{link_content}]\n____\n#{process_node_to_text(article_id, article_url, quote_node)}\n____\n\n")
      return
    end
  end

  if node.name == 'quote'
    if (node.attr('rend') == 'block') &&
        (node.xpath('./xmlns:ptr').length >= 1)
      prts_texts = []
      node.xpath('./xmlns:ptr').each do |ptr_node|
        prts_texts << get_link(article_id, ptr_node)
        ptr_node.unlink
      end
      swap_cdata(node, "\n\n[quote, #{prts_texts.join(' ')}]\n____\n#{process_node_to_text(article_id, article_url, node)}\n____\n\n")
      return
    elsif (node.attr('rend') == 'block') &&
        (node.xpath('./dhq:citRef').length == 1)
      citeref = node.at_xpath('./dhq:citRef')
      citeref_text = process_node_to_text(article_id, article_url, citeref)
      citeref.unlink
      swap_cdata(node, "\n\n[quote, #{citeref_text}]\n____\n#{process_node_to_text(article_id, article_url, node)}\n____\n\n")
      return
    elsif (node.attr('rend') == 'block') &&
        (node.xpath('./xmlns:ptr').length == 0)
      swap_cdata(node, "\n\n[quote]\n____\n#{process_node_to_text(article_id, article_url, node)}\n____\n\n")
      return
    end
  end

  if node.name == 'lg'
    if node.xpath('./xmlns:l/xmlns:ptr').length == 1
      ptr_node = node.at_xpath('./xmlns:l/xmlns:ptr')
      link_name = get_link(article_id, ptr_node)
      ptr_node.unlink
      swap_cdata(node, "\n\n[quote, #{link_name}]\n____\n#{process_node_to_text(article_id, article_url, node)}\n____\n\n")
      return
    elsif node.xpath('./xmlns:l/xmlns:ptr').length == 0
      swap_cdata(node, "\n\n[quote]\n____\n#{process_node_to_text(article_id, article_url, node)}\n____\n\n")
      return
    end
  end

  if node.name == 'figure'
    if (node.xpath('./xmlns:head').length >= 1) &&
        (node.xpath('./xmlns:graphic').length == 1) &&
        (node.attr('xml:id').nil?)
      figure_url = node.at_xpath('./xmlns:graphic').attr('url')
      figure_id = get_figure_id(article_url, figure_url)
      caption = no_break(process_node_to_text(article_id, article_url, node.xpath('./xmlns:head').last))
      swap_cdata(node, "\n\n.#{caption}\nimage::#{image_path(figure_id)}[]\n\n")
      return
    elsif (node.xpath('./xmlns:head').length == 0) &&
        (node.xpath('./xmlns:graphic').length == 1) &&
        (node.attr('xml:id'))
      figure_url = node.at_xpath('./xmlns:graphic').attr('url')
      figure_id = get_figure_id(article_url, figure_url)
      caption = node.attr('xml:id')
      swap_cdata(node, "\n\n.#{caption}\nimage::#{image_path(figure_id)}[]\n\n")
      return
    elsif (node.xpath('./xmlns:head').length >= 1) &&
        (node.xpath('./xmlns:graphic').length > 1) &&
        (node.attr('xml:id'))
      number_of_graphics = node.xpath('./xmlns:graphic').length
      text = ''
      0.upto(number_of_graphics - 2) do |graphic_index|
        figure_url = node.xpath('./xmlns:graphic')[graphic_index].attr('url')
        figure_id = get_figure_id(article_url, figure_url)
        text << "\n\n.image::#{image_path(figure_id)}[]\n\n"
      end
      figure_url = node.xpath('./xmlns:graphic')[number_of_graphics - 1].attr('url')
      figure_id = get_figure_id(article_url, figure_url)
      anchor = node.attr('xml:id')
      caption = no_break(process_node_to_text(article_id, article_url, node.xpath('./xmlns:head').last))
      swap_cdata(node, "#{text}\n\n[##{anchor}]\n.#{caption}\nimage::#{image_path(figure_id)}[]\n\n")
      return
    elsif (node.xpath('./xmlns:head').length >= 1) &&
        (node.xpath('./xmlns:graphic').length > 1) &&
        (node.attr('xml:id').nil?)
      number_of_graphics = node.xpath('./xmlns:graphic').length
      text = ''
      0.upto(number_of_graphics - 2) do |graphic_index|
        figure_url = node.xpath('./xmlns:graphic')[graphic_index].attr('url')
        figure_id = get_figure_id(article_url, figure_url)
        text << "\n\n.image::#{image_path(figure_id)}[]\n\n"
      end
      figure_url = node.xpath('./xmlns:graphic')[number_of_graphics - 1].attr('url')
      figure_id = get_figure_id(article_url, figure_url)
      caption = no_break(process_node_to_text(article_id, article_url, node.xpath('./xmlns:head').last))
      swap_cdata(node, "#{text}\n\n.#{caption}\nimage::#{image_path(figure_id)}[]\n\n")
      return
    elsif (node.xpath('./xmlns:head').length >= 1) &&
        (node.xpath('./xmlns:graphic').length == 1) &&
        (node.attr('xml:id'))
      figure_url = node.at_xpath('./xmlns:graphic').attr('url')
      figure_id = get_figure_id(article_url, figure_url)
      anchor = node.attr('xml:id')
      caption = no_break(process_node_to_text(article_id, article_url, node.xpath('./xmlns:head').last))
      swap_cdata(node, "\n\n[##{anchor}]\n.#{caption}\nimage::#{image_path(figure_id)}[]\n\n")
      return
    elsif (node.xpath('./xmlns:head').length == 0) &&
        (node.xpath('./xmlns:graphic').length == 1) &&
        (node.attr('xml:id').nil?)
      figure_url = node.at_xpath('./xmlns:graphic').attr('url')
      figure_id = get_figure_id(article_url, figure_url)
      swap_cdata(node, "\n\nimage::#{image_path(figure_id)}[]\n\n")
      return
    end
  end
  if node.name == 'table'
    if (node.xpath('./xmlns:head').length == 1) &&
        (node.xpath('./xmlns:row')[0].attr('role') == 'label') &&
        (node.xpath('./dhq:caption').length == 0)
      label_node = node.at_xpath('./xmlns:head')
      label_text = process_node_to_text(article_id, article_url, label_node)
      label_node.unlink
      node.children.each do |child|
        process_node(article_id, article_url, child)
      end
      swap_cdata(node, "\n\n[options=\"header\"]\n.#{label_text}\n|===\n#{node.text}|===\n")
      return
    elsif (node.xpath('./xmlns:head').length == 1) && (node.xpath('./dhq:caption').length == 0)
      label_node = node.at_xpath('./xmlns:head')
      label_text = process_node_to_text(article_id, article_url, label_node)
      label_node.unlink
      node.children.each do |child|
        process_node(article_id, article_url, child)
      end
      swap_cdata(node, "\n\n.#{label_text}\n|===\n#{node.text}|===\n")
      return
    elsif node.xpath('./dhq:caption').length == 1
      label_node = node.at_xpath('./dhq:caption')
      label_text = process_node_to_text(article_id, article_url, label_node)
      label_node.unlink
      node.children.each do |child|
        process_node(article_id, article_url, child)
      end
      swap_cdata(node, "\n\n.#{label_text}\n|===\n#{node.text}|===\n")
      return
    else
      node.children.each do |child|
        process_node(article_id, article_url, child)
      end
      swap_cdata(node, "\n\n|===\n#{node.text}|===\n")
      return
    end
  end

  node.children.each do |child|
    process_node(article_id, article_url, child)
  end

  if node.blank?
    node.remove
  elsif node.comment?
    node.remove
  elsif node.text?
    swap_cdata(node, node.text)
  elsif [
      'floatingText', 'body', 'citRef', 'pubPlace', 'biblScope', 'stage', 'idno', 'editor', 'publisher', 'date', 'epigraph', 'name', 'speaker', 'author'
  ].include? node.name
    swap_cdata(node, node.text)
  elsif (node.name == 'ab') && (node.attr('type') == 'imageGallery')
    swap_cdata(node, "\n#{clean_text(node.text)}\n\n")
  elsif node.name == 'p'
    swap_cdata(node, "\n#{clean_text(node.text)}\n\n")
  elsif node.name == 'ab'
    swap_cdata(node, "\n#{clean_text(node.text)}\n\n")
  elsif node.name == 'sp'
    swap_cdata(node, "\n#{clean_text(node.text)}\n\n")
  elsif node.name == 'label'
    swap_cdata(node, "\n#{clean_text(node.text)}\n\n")
  elsif node.name == 'formula'
    swap_cdata(node, "\n#{clean_text(node.text)}\n\n")
  elsif (node.name == 'title') && (node['rend'] == 'none')
    swap_cdata(node, node.text)
  elsif (node.name == 'title') && (node['rend'] == 'italic ')
    swap_cdata(node, "__#{node.text}__")
  elsif (node.name == 'title') && (node['rend'] == 'italic')
    swap_cdata(node, "__#{node.text}__")
  elsif (node.name == 'title') && (node['rend'] == 'quotes')
    swap_cdata(node, " “#{node.text}” ")
  elsif (node.name == 'title') && node.attr('rend').nil?
    swap_cdata(node, "__#{node.text}__")
  elsif node.name == 'ref'
    swap_cdata(node, "link:#{node.attr('target')}[#{node.text}]")
  elsif node.name == 'soCalled'
    swap_cdata(node, " “#{node.text}” ")
  elsif node.name == 'q'
    swap_cdata(node, " “#{node.text}” ")
  elsif node.name == 'att'
    swap_cdata(node, "``@#{node.text}``")
  elsif node.name == 'tag'
    swap_cdata(node, "``#{node.text}``")
  elsif node.name == 'cit'
    swap_cdata(node, " “#{node.text}” ")
  elsif node.name == 'said'
    swap_cdata(node, " “#{node.text}” ")
  elsif (node.name == 'quote') && (node.attr('rend') == 'inline')
    swap_cdata(node, " “#{node.text}” ")
  elsif (node.name == 'hi') && (node.attr('rend') == 'quotes')
    swap_cdata(node, " “#{node.text}” ")
  elsif (node.name == 'hi') && (node.attr('rend') == 'italic')
    swap_cdata(node, "__#{node.text}__")
  elsif (node.name == 'hi') && (node.attr('rend') == 'monospace')
    swap_cdata(node, "``#{node.text}``")
  elsif (node.name == 'hi') && (node.attr('rend') == 'bold')
    swap_cdata(node, "**#{node.text}**")
  elsif (node.name == 'hi') && (node.attr('rend') == 'superscript')
    swap_cdata(node, "^#{node.text}^")
  elsif (node.name == 'hi') && (node.attr('rend') == 'subscript')
    swap_cdata(node, "~#{node.text}~")
  elsif (node.name == 'hi') && (node.attr('rend') == 'underlined')
    swap_cdata(node, node.text)
  elsif node.name == 'ptr'
    swap_cdata(node, " #{get_link(article_id, node)} ")
  elsif node.name == 'term'
    swap_cdata(node, "__#{node.text}__")
  elsif node.name == 'emph'
    swap_cdata(node, "__#{node.text}__")
  elsif node.name == 'code'
    swap_cdata(node, "``#{node.text}``")
  elsif node.name == 'gi'
    swap_cdata(node, "``#{node.text}``")
  elsif node.name == 'note'
    swap_cdata(node, " footnote:[#{clean_text(node.text)}]")
  elsif node.name == 'head'
    if node.text.empty?
      node.unlink
    else
      swap_cdata(node, "\n=== #{node.text}\n\n")
    end
  elsif (node.name == 'item') && (node.parent.name == 'list') && (node.parent.attr('type') == 'numbered')
    swap_cdata(node, ". #{clean_text(node.text)}\n")
  elsif (node.name == 'item') && (node.parent.name == 'list') && (node.parent.attr('type') == 'ordered')
    swap_cdata(node, ". #{clean_text(node.text)}\n")
  elsif (node.name == 'item') && (node.parent.name == 'list') && (node.parent.attr('type') == 'enumerate')
    swap_cdata(node, ". #{clean_text(node.text)}\n")
  elsif (node.name == 'item') && (node.parent.name == 'list') && (node.parent.attr('type') == 'bulleted')
    swap_cdata(node, "- #{clean_text(node.text)}\n")
  elsif (node.name == 'item') && (node.parent.name == 'list') && (node.parent.attr('type') == 'unordered')
    swap_cdata(node, "- #{clean_text(node.text)}\n")
  elsif (node.name == 'item') && (node.parent.name == 'list') && (node.parent.attr('type') == 'simple')
    swap_cdata(node, "- #{clean_text(node.text)}\n")
  elsif (node.name == 'item') && (node.parent.name == 'list') && (node.parent.attr('type') == 'gloss')
    swap_cdata(node, "- #{clean_text(node.text)}\n")
  elsif (node.name == 'item') && (node.parent.name == 'list') && (node.parent.attr('type') == 'glossed')
    swap_cdata(node, "- #{clean_text(node.text)}\n")
  elsif (node.name == 'item') && (node.parent.name == 'list') && (node.parent.attr('type').nil?)
    swap_cdata(node, "- #{clean_text(node.text)}\n")
  elsif (node.name == 'list') && (node.attr('type').nil?)
    swap_cdata(node, "\n#{node.text}\n")
  elsif (node.name == 'list') && (node.attr('type') == 'bulleted')
    swap_cdata(node, "\n#{node.text}\n")
  elsif (node.name == 'list') && (node.attr('type') == 'numbered')
    swap_cdata(node, "\n#{node.text}\n")
  elsif (node.name == 'list') && (node.attr('type') == 'ordered')
    swap_cdata(node, "\n#{node.text}\n")
  elsif (node.name == 'list') && (node.attr('type') == 'enumerate')
    swap_cdata(node, "\n#{node.text}\n")
  elsif (node.name == 'list') && (node.attr('type') == 'unordered')
    swap_cdata(node, "\n#{node.text}\n")
  elsif (node.name == 'list') && (node.attr('type') == 'simple')
    swap_cdata(node, "\n#{node.text}\n")
  elsif (node.name == 'list') && (node.attr('type') == 'gloss')
    swap_cdata(node, "\n#{node.text}\n")
  elsif (node.name == 'list') && (node.attr('type') == 'glossed')
    swap_cdata(node, "\n#{node.text}\n")
  elsif node.name == 'div'
    swap_cdata(node, "#{clean_text(node.text)}\n\n")
  elsif node.name == 'anchor'
    swap_cdata(node, "\n\n[#{node.attr('xml:id')}-#{article_id}]\n")
  elsif node.name == 'lb'
    swap_cdata(node, "#{node.text} +\n")
  elsif node.name == 'l'
    swap_cdata(node, "#{node.text} +\n")
  elsif node.name == 'row'
    swap_cdata(node, "#{node.text}\n")
  elsif node.name == 'cell'
    swap_cdata(node, "|#{node.text}")
  elsif node.name == 'foreign'
    swap_cdata(node, node.text)
  elsif node.name == 'eg'
    swap_cdata(node, "\n\n[quote]\n____\n```\n#{node.text}\n```\n____\n")
  elsif node.name == 'example'
    swap_cdata(node, "\n\n[quote]\n____\n#{node.text}\n____\n")
  elsif node.name == 'graphic'
    figure_url = node.attr('url')
    figure_id = get_figure_id(article_url, figure_url)
    swap_cdata(node, " image:[figures/#{figure_id}]")
  elsif node.name == 'bibl'
    if node.attr('xml:id')
      swap_cdata(node, "- [[#{node.attr('xml:id')}-#{article_id}]] [#{node.attr('xml:id')}-#{article_id}] #{no_break(clean_text(node.text))}\n")
    else
      node.remove
    end
  else
    raise "Unknown node [#{node}]"
  end
end

issues = JSON.parse(IO.read(ARTICLES_WITH_ID_JSON_FILE))
book_issues = []
issue_index = 0
issues.each_pair do |issue_name, issue|
  open(File.join(BOOK_DIR, "0-#{issue_index}.adoc"), 'w') do |issue_content|
    issue_content << "[#0-#{issue_index}]\n"
    issue_content << "= #{issue_name.strip}\n"
    all_authors = Set.new

    articles_lists = []
    issue['articles'].each do |article|
      if article.key? 'index'
        article_index = article['index']
        if article_index >= 0
          p "Article #{article_index}"
          article_xml_content = Nokogiri::XML(IO.read(File.join(ARTICLES_DIR, "#{article_index}.xml")))
          authors_names = article_xml_content.xpath('//dhq:author_name').collect do |name|
            all_authors << name.text.strip
            name.text.strip
          end.join('; ').tr("\n", ' ').gsub(/\s+/, ' ')
          open(File.join(BOOK_DIR, "#{article_index}.adoc"), 'w') do |article_content|
            article_content << "[##{article_index}]\n"
            article_content << "== #{article['title'].strip}\n"
            article_content << ":author: #{authors_names}\n"
            article_content << ":lang: en\n"
            description_text = clean_text(article_xml_content.at_xpath('//dhq:teaser').text.strip)
            if description_text != ''
              article_content << ":description: #{description_text}\n"
            end
            article_content << "\n"
            print_content_article(article_index, article['url'], article_xml_content, article_content)
          end
          articles_lists << "#{article_index}.adoc"
        end

      end
    end

    issue_content << ":author: #{all_authors.to_a.join('; ').tr("\n", ' ').gsub(/\s+/, ' ')}\n"
    issue_content << ":lang: en\n"
    issue_content << "\n"
    articles_lists.each do |article|
      issue_content << "include::#{article}[]\n"
    end

  end
  book_issues << issue_index
  issue_index += 1
end

time = Time.new
version_string = "v1.0, #{time.year}-#{time.month}-#{time.day}\n"

open(File.join(BOOK_DIR, 'index.adoc'), 'w') do |main_book_content|
  main_book_content << "= DHQ: Digital Humanities Quarterly\n"
  main_book_content << "DHQ\n"
  main_book_content << version_string
  main_book_content << ":doctype: book\n"
  main_book_content << ":lang: en\n"
  if File.exist? 'data/dhqlogo.png'
    main_book_content << ":front-cover-image: image:../dhqlogo.png[width=284,height=129]\n"
  end
  main_book_content << "\n"
  book_issues.each do |issue|
    main_book_content << "include::0-#{issue}.adoc[]\n"
  end
end

IO.write(FIGURES_JSON_FILE, JSON.pretty_generate(FIGURES))

p 'Downloading'
easy_options = {:follow_location => true, :multipart_form_post => true}
multi_options = {}
missing_urls = FIGURES.keys.find_all do |image_url|
  image_file = FIGURES[image_url]
  !File.exist?(File.join(FIGURES_DIR, image_file))
end

unless missing_urls.empty?
  Curl::Multi.get(missing_urls, easy_options, multi_options) do |easy|
    if easy.status == '200 OK'
      http_headers = easy.header_str.split(/[\r\n]+/).map(&:strip)
      http_headers = Hash[http_headers.flat_map {|s| s.scan(/^(\S+): (.+)/)}]

      if http_headers['Content-Type'] != 'text/html'
        IO.write(File.join(FIGURES_DIR, FIGURES[easy.url]), easy.body_str, mode: 'w')
      else
        p "Bad file [#{easy.url}]"
      end
    end
  end
end

p 'Converting images'
image_convert_files = File.join(DATA_DIR, 'convert_images.sh')
open(image_convert_files, 'w') do |conversion_file|
  FIGURES.values.each do |figure|
    if File.exist?("data/figures/#{figure}") && (!File.exist?("data/converted/#{figure}"))
      conversion_file << "#{IMAGE_TRANSFORMATION_SCRIPT} data/figures/#{figure} data/converted/#{figure}\n"
    end
  end
end
`gm batch -stop-on-error on #{image_convert_files}`

p 'Converting book'
if File.exists? 'out.log'
  File.unlink 'out.log'
end
if File.exists? 'out2.log'
  File.unlink 'out2.log'
end

`asciidoctor-epub3 -D output data/book/index.adoc >>out.log 2>&1`
`asciidoctor-epub3 -D output -a ebook-format=kf8 data/book/index.adoc >>out2.log 2>&1`
