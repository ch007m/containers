Vagrant.configure("2") do |config|
  config.vm.box = "generic/fedora33"
  config.vm.hostname = "fedora-podman"
  config.vm.provider "virtualbox" do |v|
    v.memory = 4096
    v.cpus = 4
  end

  config.vm.provision "shell", inline: <<-SHELL
    dnf update
    dnf --enablerepo=updates-testing install podman libvarlink-util libvarlink -y
  SHELL
end
