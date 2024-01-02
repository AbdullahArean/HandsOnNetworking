import os

# Get the directory path of the current script
current_dir = os.path.dirname(os.path.realpath(__file__))

# Get the path of the LinkStateRouting.py file (assuming it's in the parent directory's src folder)
lsr_path = os.path.join(current_dir, "..", "src", "DistanceVectorRouting.py")

# List of config files to run
configs = ['configA.txt', 'configB.txt', 'configC.txt', 'configD.txt', 'configE.txt', 'configF.txt']

# Loop through each config file and open a new terminal window for each one
for i, config in enumerate(configs):
    config = os.path.join(current_dir, config)
    # Use the command 'gnome-terminal' to open a new terminal window with a specific title and run the python command
    # with the configuration
    os.system('gnome-terminal --title="{title}" -e "python3 {lsr_path} {config}" &'.format(title=chr(65 + i), lsr_path=lsr_path, config=config))