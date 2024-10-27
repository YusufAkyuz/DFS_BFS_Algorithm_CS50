import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java Maze maze.txt");
            return;
        }

        try {
            Maze maze = new Maze(args[0]);
            System.out.println("Maze:");
            maze.print();
            System.out.println("Solving...");
            maze.solve();
            System.out.println("Solution:");
            maze.print();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
class Node {
    public int[] state;
    public Node parent;
    public String action;

    public Node(int[] state, Node parent, String action) {
        this.state = state;
        this.parent = parent;
        this.action = action;
    }
}

class StackFrontier {
    public List<Node> frontier = new ArrayList<>();

    public void add(Node node) {
        frontier.add(node);
    }

    public boolean containsState(int[] state) {
        for (Node node : frontier) {
            if (Arrays.equals(node.state, state)) {
                return true;
            }
        }
        return false;
    }

    public boolean empty() {
        return frontier.isEmpty();
    }

    public Node remove() throws Exception {
        if (empty()) {
            throw new Exception("empty frontier");
        } else {
            return frontier.removeLast();           // For -> Last in first out
        }
    }
}

class QueueFrontier extends StackFrontier {
    @Override
    public Node remove() throws Exception {
        if (empty()) {
            throw new Exception("empty frontier");
        } else {
            return frontier.removeFirst();          // For -> First in first out
        }
    }
}

class Maze {
    private int height, width;
    private int[] start;
    private int[] goal;
    private boolean[][] walls;
    private List<int[]> solutionPath;

    public Maze(String filename) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filename));
        //Correct
        if (lines.stream().filter(line -> line.contains("A")).count() != 1 ||
                lines.stream().filter(line -> line.contains("B")).count() != 1) {
            throw new IllegalArgumentException("Maze must have exactly one start point and one goal.");
        }

        this.height = lines.size();
        this.width = lines.get(0).length();
        this.walls = new boolean[height][width];

        for (int i = 0; i < height; i++) {
            String line = lines.get(i);
            for (int j = 0; j < width; j++) {
                char ch = line.charAt(j);
                if (ch == 'A') {
                    this.start = new int[]{i, j};
                    walls[i][j] = false;
                } else if (ch == 'B') {
                    this.goal = new int[]{i, j};
                    walls[i][j] = false;
                } else if (ch == ' ') {
                    walls[i][j] = false;
                } else {
                    walls[i][j] = true;
                }
            }
        }
    }

    public void print() throws InterruptedException {
        System.out.println();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (walls[i][j]) {
                    System.out.print("█");
                } else if (Arrays.equals(new int[]{i, j}, start)) {
                    System.out.print("A");
                } else if (Arrays.equals(new int[]{i, j}, goal)) {
                    System.out.print("B");
                } else if (solutionPath != null && containsInSolution(i, j)) {
                    System.out.print("*");
                } else {
                    System.out.print(" ");
                }
            }
            System.out.println();
        }
    }

    private boolean containsInSolution(int row, int col) {
        for (int[] cell : solutionPath) {
            if (cell[0] == row && cell[1] == col) {
                return true;
            }
        }
        return false;
    }

    public List<int[]> neighbors(int[] state) {
        int row = state[0];
        int col = state[1];
        List<int[]> result = new ArrayList<>();

        int[][] candidatePositions = {
                {row - 1, col}, // up
                {row + 1, col}, // down
                {row, col - 1}, // left
                {row, col + 1}  // right
        };

        for (int[] candidate : candidatePositions) {
            int r = candidate[0];
            int c = candidate[1];
            if (r >= 0 && r < height && c >= 0 && c < width && !walls[r][c]) {
                result.add(candidate);
            }
        }
        return result;
    }

    public void solve() throws Exception {
        StackFrontier frontier = new StackFrontier();
        frontier.add(new Node(start, null, null));
        Set<String> explored = new HashSet<>();

        while (true) {
            if (frontier.empty()) {
                throw new Exception("No solution found.");
            }

            Node node = frontier.remove();

            if (Arrays.equals(node.state, goal)) {
                solutionPath = new ArrayList<>();       // For solution path
                while (node.parent != null) {
                    solutionPath.add(node.state);
                    node = node.parent;
                }
                //Collections.reverse(solution); yapılırsa: solution listesi başlangıçtan hedefe doğru sıralanır. Yolu adım adım takip etmek için doğru sırada bilgi elde edilir.
                //Collections.reverse(solution); yapılmazsa: Çözüm yolu tersten kalır ve yol hedeften başlayarak başlangıca doğru görünür.
                // Bu durumda çözüm yolu ters olur ve kullanıcıya veya diğer sistemlere bu sırada sunulması kafa karışıklığı yaratabilir.
               Collections.reverse(solutionPath);
                return;
            }

            explored.add(Arrays.toString(node.state));

            for (int[] state : neighbors(node.state)) {
                if (!frontier.containsState(state) && !explored.contains(Arrays.toString(state))) {
                    frontier.add(new Node(state, node, null));
                }
            }
        }
    }
}
