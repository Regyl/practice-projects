import java.util.HashSet;
import java.util.Set;

class Solution {

    private static final char ISLAND = '1';
    
    public static void main(String[] args) {
        char[][] grid = {
                {'1','1','1','1','0'},
  {'1','1','0','1','0'},
  {'1','1','0','0','0'},
  {'0','0','0','0','0'}
        };
        
        char[][] grid1 = {
                {'1','1','0','0','0'},
  {'1','1','0','0','0'},
  {'0','0','1','0','0'},
  {'0','0','0','1','1'}
        };
        
        char[][] grid2 = {
                ["1","0","1","1","1"],
                ["1","0","1","0","1"],
                ["1","1","1","0","1"]
        }
        Solution solution = new Solution();
        solution.numIslands(grid1);
    }

    public int numIslands(char[][] grid) {
        Set<Set<GraphNode>> set = new HashSet<>();
        
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                char item = grid[i][j];
                if (item != ISLAND) {
                    continue;
                }

                boolean foundBool = false;
                GraphNode node = new GraphNode(i,j);
                Set<GraphNode> neighbours = getNeighbours(node, grid);
                neighbours.add(node);
                Set<GraphNode> found = null;
                for (Set<GraphNode> graph : set) {
                    for (GraphNode neighbour : neighbours) {
                        if (graph.contains(neighbour)) {
                            found = graph;
                            foundBool = true;
                            break;
                        }
                    }
                    if (foundBool) {
                        break;
                    }
                }

                if (!foundBool) {
                    set.add(neighbours);
                } else {
                    found.addAll(neighbours);
                }
            }
        }

        return set.size();
    }

    private Set<GraphNode> getNeighbours(GraphNode node, char[][] grid) {
        int maxX = grid.length - 1;
        int maxY = grid[0].length - 1;
        int x = node.x;
        int y = node.y;

        Set<GraphNode> graph = new HashSet<>();
        int x1 = x - 1;
        int y1 = y;

        int x2 = x + 1;
        int y2 = y;

        int x3 = x;
        int y3 = y - 1;

        int x4 = x;
        int y4 = y + 1;

        if (isValid(x1, y1, maxX, maxY, grid)) {
            graph.add(new GraphNode(x1, y1));
        }
        if (isValid(x2, y2, maxX, maxY)) {
            graph.add(new GraphNode(x2, y2));
        }
        if (isValid(x3, y3, maxX, maxY)) {
            graph.add(new GraphNode(x3, y3));
        }
        if (isValid(x4, y4, maxX, maxY)) {
            graph.add(new GraphNode(x4, y4));
        }
        return graph;
    }

    private boolean isValid(int x, int y, int maxX, int maxY) {
        return x >= 0 && y >= 0 && x <= maxX && y <= maxY;
    }
    
    private static class GraphNode {
        final int x;
        final int y;

        public GraphNode(int x, int y) {
            this.x = x;
            this.y = y;
        }
        
        @Override
        public int hashCode() {
            return x + y;
        }
        
        @Override
        public boolean equals(Object obj) {
            return obj instanceof GraphNode node && node.x == x && node.y == y;
        }
    }
}