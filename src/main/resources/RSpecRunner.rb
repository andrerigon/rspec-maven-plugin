require 'rubygems'
require 'rspec/core'

class RSpecRunnerWithArgsFix <  RSpec::Core::Runner
    def self.autorun_with_args(*args)
      return if autorun_disabled? || installed_at_exit? || running_in_drb?
      @installed_at_exit = true 
      run(args, $stderr, $stdout)
    end
end

def run(sourceDir, reportFile)
  RSpecRunnerWithArgsFix.autorun_with_args( sourceDir, '-f', 'html', '-o', reportFile )
end
