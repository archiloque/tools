require 'rubygems'
require 'process/XXX_common'
require 'faster_csv'

# param: year, critic id and number of films
QUERY_CRITIC_GENRES = "select count(*) c, sum(value) s, (CAST(sum(value) AS FLOAT)/ count(*)) a, ge.name n, ge.id i " +
    "from genres ge, films_genres fg, grades gr, films f " +
    "where strftime('%Y', pub_date) = ? and gr.film_id = fg.film_id and fg.genre_id = ge.id and gr.critic_id = ? and f.id = fg.film_id " +
    "group by ge.id " +
    "having c >= ? "

FasterCSV.open("out/data.csv", "w", {:col_sep => ","}) do |csv|

  Critic.order(:name).each do |critic|
    grades_per_year_per_genre = Hash.new { |hash, key| hash[key] = (Hash.new { |h, k| h[k] = {} }) }


    2000.upto(2010) do |year|
      DB.fetch(QUERY_CRITIC_GENRES, year.to_s, critic.id, 10) do |row|
        grades_per_year_per_genre[year][row[:n]] = row[:a]
      end
    end
    head = [critic.name]

    2000.upto(2010) do |year|
      head << year.to_s
    end
    csv << head

    grades_per_year_per_genre.each_pair do |genre, years|
      line = [genre]
      2000.upto(2010) do |year|
        line << (years[year])
      end
      csv << line
    end
    csv << []
  end
end
