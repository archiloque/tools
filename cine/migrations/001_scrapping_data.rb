class ScrappingData < Sequel::Migration
  def up
    create_table(:films) do
      primary_key :id
      String :title, :null => false
      Date :pub_date, :null =>false, :index => true
      Fixnum :custom_id, :null => false, :unique => true, :index => true, :unsigned => true
      
      Boolean :scrapped, :index => true, :default => false

      Float :average_press, :null => true
      Float :average_people, :null => true

      Fixnum :percent_people_1, :null => true
      Fixnum :percent_people_2, :null => true
      Fixnum :percent_people_3, :null => true
      Fixnum :percent_people_4, :null => true
      Fixnum :percent_people_5, :null => true
    end

    create_table(:genres) do
      primary_key :id
      String :name, :null => false, :unique => true, :index => true
      Fixnum :custom_id, :null => false, :unique => true, :index => true, :unsigned => true
    end

    create_table(:countries) do
      primary_key :id
      String :name, :null => false, :unique => false, :index => true
      Fixnum :custom_id, :null => false, :unique => true, :index => true, :unsigned => true
    end

    create_table(:directors) do
      primary_key :id
      String :name, :null => false, :unique => false, :index => true
      Fixnum :custom_id, :null => false, :unique => true, :index => true, :unsigned => true
    end

    create_table(:tags) do
      primary_key :id
      String :name, :null => false, :unique => false, :index => true
      Fixnum :custom_id, :null => false, :unique => true, :index => true, :unsigned => true
    end

    create_table(:critics) do
      primary_key :id
      String :name, :null => false, :unique => true, :index => true
    end

    create_table(:countries_films) do
      foreign_key :film_id, :films, :index => true, :null => false
      foreign_key :country_id, :countries, :index => true, :null => false
    end

    create_table(:directors_films) do
      foreign_key :film_id, :films, :index => true, :null => false
      foreign_key :director_id, :directors, :index => true, :null => false
    end

    create_table(:films_genres) do
      foreign_key :film_id, :films, :index => true, :null => false
      foreign_key :genre_id, :genres, :index => true, :null => false
    end

    create_table(:films_tags) do
      foreign_key :film_id, :films, :index => true, :null => false
      foreign_key :tag_id, :tags, :index => true, :null => false
    end

    create_table(:grades) do
      foreign_key :film_id, :films, :index => true, :null => false
      foreign_key :critic_id, :critics, :index => true, :null => false
      Fixnum :value, :null => false, :unsigned => true, :index => true
    end
  end

  def down
    drop_table(:grades)
    drop_table(:films_tags)
    drop_table(:films_genres)
    drop_table(:countries_films)

    drop_table(:critics)
    drop_table(:tags)
    drop_table(:directors)
    drop_table(:countries)
    drop_table(:genres)
    drop_table(:films)
  end
end