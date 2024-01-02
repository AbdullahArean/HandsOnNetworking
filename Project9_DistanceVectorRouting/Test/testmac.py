import os

# Get the directory path of the current script
current_dir = os.path.dirname(os.path.realpath(__file__))

# Get the path of the LinkStateRouting.py file (assuming it's in a directory called "src")
lsr_path = os.path.join(current_dir, "..", "src", "DistanceVectorRouting.py")

# List of config files to run
configs = ['configA.txt', 'configB.txt', 'configC.txt', 'configD.txt', 'configE.txt', 'configF.txt']

# Loop through each config file and open a new terminal window for each one
for config in configs:
    # Change the working directory of the terminal to the current directory of the Python file
    os.system(f"osascript -e 'tell app \"Terminal\" to do script \"cd {current_dir} && python3 {lsr_path} {config}\"'")
