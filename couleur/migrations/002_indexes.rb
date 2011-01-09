Sequel.migration do
  up do
    alter_table(:articles) do
      add_index :image_url, :unique => false
      add_index :colors, :unique => false
    end
  end

  down do
    alter_table(:articles) do
      drop_index :image_url
      drop_index :colors
    end
  end
end
