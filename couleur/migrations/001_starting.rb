Sequel.migration do
  up do
    create_table(:articles) do
      primary_key :id
      String :url, :null => false
      index :url, :unique => true

      String :image_url, :null => true

      Boolean :fetched, :null => false, :default => false
      index :fetched, :unique => false

      String :colors, :null => true
    end
    create_table(:tags) do
      primary_key :id

      String :text, :null => false

      String :url_fragment, :null => false
      index :url_fragment, :unique => true

      Boolean :fetched, :null => false, :default => false
      index :fetched, :unique => false
    end
    create_table(:articles_tags) do
      foreign_key :article_id, :articles
      foreign_key :tag_id, :tags
    end
  end

  down do
    drop_table(:articles_tags)
    drop_table(:tags)
    drop_table(:articles)
  end
end
