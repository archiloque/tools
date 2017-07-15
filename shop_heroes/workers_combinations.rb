#!/usr/bin/env ruby

require 'terminal-table'
require 'set'
require_relative 'workers_info'


def create_table(number_of_workers, basic_workers, base_skills)
  target_skills = base_skills ? BASE_SKILLS : ALL_SKILLS
  table = Terminal::Table.new(:headings => ["#{number_of_workers}#{basic_workers ? ' tiers 1' : ''} workers"] + target_skills ) do |t|
    (basic_workers ? TIERS_1_WORKERS : (TIERS_1_WORKERS + TIERS_2_WORKERS)).combination(number_of_workers) do |combination|
      current_line = [combination.sort.join(', ')]
      workers_skills = Set.new
      combination.each do |worker|
        workers_skills = workers_skills | WORKERS_SKILLS[worker]
      end
      if base_skills
        workers_skills = workers_skills & BASE_SKILLS
      end
      current_line = ["#{(workers_skills.length == target_skills.length) ? "✓" : " "} #{combination.sort.join(', ')}"]
      target_skills.each do |s|
        current_line << ((workers_skills.include? s) ? "✓" : "")
      end
      t << current_line
    end
  end
  puts table
  STDOUT << "\n"
end

1.upto(6) do |number_of_workers|
  create_table(number_of_workers, true, true)
  create_table(number_of_workers, false, true)
  create_table(number_of_workers, false, false)
end
