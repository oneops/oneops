class CreateMiscDocs < ActiveRecord::Migration
  def up
    create_table :misc_docs do |t|
      t.string :name, :limit => 50
      t.text :document

      t.timestamps
    end
    add_index :misc_docs, [:name], :unique => true
  end

  def down
    drop_table :misc_docs
  end
end
