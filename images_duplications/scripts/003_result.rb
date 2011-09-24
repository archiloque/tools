require_relative '../lib/models'
require 'erb'
require 'set'

DUPLICATE_LEVEL = 0.8125

dupes_id = Set.new
results = []

Article.filter('fingerprint is not null').order(:id.asc).each do |article|
  if dupes_id.include? article.id
    dupes_id.delete article.id
  else
    similars = Article.filter('fingerprint is not null').filter('id > ?', article.id).filter('hamming(fingerprint, (select fingerprint from articles where id = ?)) >= ?', article.id, DUPLICATE_LEVEL)
    if similars.count > 0
      r = [article.url]
      similars.each do |s|
        dupes_id << s.id
        r << s.url
      end
      results << [r, article.image_path]
    end
  end
end

results.sort! { |a, b| b[0].size <=> a[0].size }

STDOUT << ERB.new(File.new('scripts/003.erb').read).result(binding)
