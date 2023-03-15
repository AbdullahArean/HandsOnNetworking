import matplotlib.pyplot as plt

def extract_points(filename):
    with open(filename, "r") as f:
        data = f.read()
    points = [(float(x), float(y)) for x, y in [p.split(",") for p in data.split("\n") if p]]
    return zip(*points)

# Extract the points from the files
x_coords1, y_coords1 = extract_points("pointstahoe.txt")
x_coords2, y_coords2 = extract_points("pointsreno.txt")

# Create a new figure object with high DPI
fig = plt.figure(dpi=150)

# Plot the first set of points in blue and the second set in red
plt.plot(y_coords1, x_coords1, 'bo', label='TCPTahoe')
plt.plot(y_coords2, x_coords2, 'ro', label='TCPReno')

# Set the axis labels and title
plt.xlabel('Transmission Round')
plt.ylabel('Congestion Window Size')
plt.title('TCP Tahoe TCP Reno Congestion Windows')

# Add a legend to the graph
plt.legend()

# Show the graph
plt.show()
plt.savefig('tcp_plot.jpg', dpi=300, bbox_inches='tight')
