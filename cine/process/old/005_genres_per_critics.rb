require 'rubygems'
require 'database'

DB.transaction do
  Genre.each do |genre|
    # all grades that concern this genre per critic
    DB.fetch("select critic_id cr, sum(value) v, count(*) co from grades where film_id in (select film_id from films_genres where films_genres.genre_id = ?) group by critic_id", genre.id) do |row|
      GenresPerCritic.create(:critic_id => row[:cr], :genre_id => genre.id, :number => row[:co], :average => (row[:v].to_f / row[:co].to_f))
    end
  end
end

