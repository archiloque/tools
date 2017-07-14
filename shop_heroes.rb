#!/usr/bin/env ruby

require 'terminal-table'
require 'set'

SKILL_ARMOR_CRAFTING = "armor"
SKILL_METAL_WORKING = "metal"
SKILL_WEAPON_CRAFTING = "weapon"
SKILL_WOOD_WORKING = "wood"
SKILL_ALCHEMY = "alchemy"
SKILL_TEXTILE_WORKING = "textile"
SKILL_ARTS_AND_CRAFT = "arts"

SKILLS = [
  SKILL_ARMOR_CRAFTING,
  SKILL_METAL_WORKING,
  SKILL_WEAPON_CRAFTING,
  SKILL_WOOD_WORKING,
  SKILL_ALCHEMY,
  SKILL_TEXTILE_WORKING,
  SKILL_ARTS_AND_CRAFT
]

WORKER_ARMORER = "armorer"
WORKER_BLACKSMITH = "blacksmith"
WORKER_CARPENTER = "carpenter"
WORKER_DRUID = "druid"
WORKER_LEATHER_WORKER = "leather worker"
WORKER_TAILOR = "tailor"
WORKER_FLETCHER = "fletcher"


WORKERS_SKILLS = {
  WORKER_ARMORER => [SKILL_ARMOR_CRAFTING, SKILL_METAL_WORKING],
  WORKER_BLACKSMITH => [SKILL_METAL_WORKING, SKILL_WEAPON_CRAFTING],
  WORKER_CARPENTER => [SKILL_WEAPON_CRAFTING, SKILL_WOOD_WORKING],
  WORKER_DRUID => [SKILL_ALCHEMY, SKILL_WOOD_WORKING],
  WORKER_LEATHER_WORKER => [SKILL_ARMOR_CRAFTING, SKILL_TEXTILE_WORKING],
  WORKER_TAILOR => [SKILL_ARTS_AND_CRAFT, SKILL_TEXTILE_WORKING],
  WORKER_FLETCHER => [SKILL_ARTS_AND_CRAFT, SKILL_WEAPON_CRAFTING]
}

1.upto(5) do |number_of_workers|
  table = Terminal::Table.new(:headings => ["#{number_of_workers} workers", "All skils ?"] + SKILLS ) do |t|
    WORKERS_SKILLS.keys.to_a.combination(number_of_workers) do |combination|
      current_line = [combination.sort.join(', ')]
      workers_skills = Set.new
      combination.each do |worker|
        workers_skills = workers_skills | WORKERS_SKILLS[worker]
      end
      current_line << ((workers_skills.length == SKILLS.length) ? "✓" : "")
      SKILLS.each do |s|
        current_line << ((workers_skills.include? s) ? "✓" : "")
      end
      t << current_line
    end
  end
  puts table
  STDOUT << "\n"
end
