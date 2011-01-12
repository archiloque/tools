require 'rubygems'
require 'sequel'
require 'logger'

DB = Sequel.connect('sqlite://cine.sqlite') #, :loggers => [Logger.new($stdout)])

Sequel.inflections do |inflect|
   inflect.irregular 'country', 'countries'
end

class Film < Sequel::Model
  many_to_many :tags
  many_to_many :genres
  one_to_many :grades
  many_to_many :directors
  many_to_many :countries
end

class Genre < Sequel::Model
  many_to_many :films
end

class Country < Sequel::Model
  many_to_many :films
end

class Director < Sequel::Model
  many_to_many :films
end

class Tag < Sequel::Model
  many_to_many :films
end

class Critic < Sequel::Model
  one_to_many :grades
end

class Grade < Sequel::Model
  many_to_one :film
  many_to_one :critic
end
