Sequel.migration do
  up do
    alter_table(:articles) do
      add_column :fingerprint, 'BIT(64)', :null => true
    end
  end
end