require_relative '../lib/models'
require 'phashion'

Article.filter('image_url is not null').filter('fingerprint is null').each do |article|
  if File.exist? article.image_path
    begin
      fingerprint = Phashion::Image.new(article.image_path).fingerprint
      article.update({:fingerprint => Sequel::LiteralString.new("B'#{fingerprint.to_s(2).rjust(64, '0')}'")})
    rescue RuntimeError => e
      p e
    end
  end
end