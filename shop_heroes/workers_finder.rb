#!/usr/bin/env ruby

require 'terminal-table'
require 'set'
require_relative 'workers_info'

number_of_slots = 4
available_workers = TIERS_1_WORKERS + [
  WORKER_FLETCHER,
  WORKER_ARTIFICER,
  WORKER_LUTHIER,
  WORKER_JEWELER
]

def count_workers_by_tier(workers, workers_list)
  workers.count do |w|
    workers_list.include?(w)
  end
end

only_tiers_1_workers = count_workers_by_tier(available_workers, TIERS_1_WORKERS) == available_workers.length

available_skills_for_me_set = Set.new
available_workers.each do |worker|
  WORKERS_SKILLS[worker].each do |skill|
    available_skills_for_me_set << skill
  end
end

available_skills_for_me = available_skills_for_me_set.to_a
unless only_tiers_1_workers
  available_skills_for_me = available_skills_for_me - TIERS_1_SKILLS.to_a
end

results = []

available_workers.combination(number_of_slots) do |combination|
  current_line = [combination.sort.join(', ')]
  workers_skills = Set.new
  combination.each do |worker|
    workers_skills = workers_skills | WORKERS_SKILLS[worker]
  end
  if only_tiers_1_workers || ((workers_skills & TIERS_1_SKILLS).length == TIERS_1_SKILLS.length)
    results << {
      workers: combination,
      skills: workers_skills,
      tiers_1_workers: count_workers_by_tier(combination, TIERS_1_WORKERS),
      tiers_2_workers: count_workers_by_tier(combination, TIERS_2_WORKERS),
    }
  end
end

results.sort! do |x,y|
  if y[:skills].length != x[:skills].length
    y[:skills].length <=> x[:skills].length
  else
    x[:tiers_2_workers] <=> y[:tiers_2_workers]
  end
end

table = Terminal::Table.new(:headings => ["workers", "skills", "tiers 1", "tiers 2"] + available_skills_for_me ) do |t|
  results.each do |result|
    current_line = [
      result[:workers].sort.join(', '),
      result[:skills].length,
      result[:tiers_1_workers],
      result[:tiers_2_workers]
    ]
    available_skills_for_me.each do |s|
      #current_line << s
      current_line << ((result[:skills].include? s) ? "✓" : "")
    end
    t << current_line
  end
end
puts table
