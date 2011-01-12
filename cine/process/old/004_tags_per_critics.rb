require 'rubygems'
require 'database'

DB.transaction do
  Tag.each do |tag|
    # all grades that concern this tag per critic
    DB.fetch("select critic_id cr, sum(value) v, count(*) co from grades where film_id in (select film_id from films_tags where films_tags.tag_id = ?) group by critic_id", tag.id) do |row|
      TagsPerCritic.create(:critic_id => row[:cr], :tag_id => tag.id, :number => row[:co], :average => (row[:v].to_f / row[:co].to_i))
    end
  end
end