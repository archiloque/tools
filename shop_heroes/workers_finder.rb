#!/usr/bin/env ruby

require 'terminal-table'
require 'set'
require_relative 'workers_info'

number_of_slots = 4
available_workers = TIERS_1_WORKERS + [
  WORKER_FLETCHER,
  WORKER_ARTIFICER,
]

available_skills_for_me_set = Set.new
available_workers.each do |worker|
  WORKERS_SKILLS[worker].each do |skill|
    available_skills_for_me_set << skill
  end
end

available_skills_for_me = available_skills_for_me_set.to_a

results = []

def count_workers_by_tier(workers, workers_list)
  workers.count do |w|
    workers_list.include?(w)
  end
end

available_workers.combination(number_of_slots) do |combination|
  current_line = [combination.sort.join(', ')]
  workers_skills = Set.new
  combination.each do |worker|
    workers_skills = workers_skills | WORKERS_SKILLS[worker]
  end
  results << {workers: combination, skills: workers_skills}
end

results.sort!{ |x,y| y[:skills].length <=> x[:skills].length }

table = Terminal::Table.new(:headings => ["workers", "skills", "tiers 1", "tiers 2"] + available_skills_for_me ) do |t|
  results.each do |result|
    current_line = [
      result[:workers].sort.join(', '),
      result[:skills].length,
      count_workers_by_tier(result[:workers], TIERS_1_WORKERS),
      count_workers_by_tier(result[:workers], TIERS_2_WORKERS),
    ]
    available_skills_for_me.each do |s|
      #current_line << s
      current_line << ((result[:skills].include? s) ? "✓" : "")
    end
    t << current_line
  end
end
puts table
