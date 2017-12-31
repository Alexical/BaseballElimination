import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.princeton.cs.algs4.FlowEdge;
import edu.princeton.cs.algs4.FlowNetwork;
import edu.princeton.cs.algs4.FordFulkerson;
import edu.princeton.cs.algs4.In;

public class BaseballElimination {

    private final int n;
    private final String[] teams;
    private final int[] w, d, r;
    private final int[][] g;
    private final Map<String, Integer> teamIndex;
    private final Map<String, Iterable<String>> certificates;

    public BaseballElimination(String filename) {
        In in = new In(filename);

        n = in.readInt();
        teams = new String[n];
        w = new int[n];
        d = new int[n];
        r = new int[n];
        g = new int[n][n];

        for (int i = 0; i < n; ++i) {
            teams[i] = (in.readString());
            w[i] = in.readInt();
            d[i] = in.readInt();
            r[i] = in.readInt();
            for (int j = 0; j < n; ++j)
                g[i][j] = in.readInt();
        }

        teamIndex = new HashMap<>();
        for (int i = 0; i < n; ++i)
            teamIndex.put(teams[i], i);

        certificates = new HashMap<>();
    }

    public int numberOfTeams() { return n; }

    public Iterable<String> teams() { return Arrays.asList(teams); }

    public int wins(String team) {
        if (teamIndex.containsKey(team))
            return w[teamIndex.get(team)];
        else throw new IllegalArgumentException();
    }

    public int losses(String team) {
        if (teamIndex.containsKey(team))
            return d[teamIndex.get(team)];
        else throw new IllegalArgumentException();
    }

    public int remaining(String team) {
        if (teamIndex.containsKey(team))
            return r[teamIndex.get(team)];
        else throw new IllegalArgumentException();
    }

    public int against(String team1, String team2) {
        if (teamIndex.containsKey(team1) && teamIndex.containsKey(team2))
            return g[teamIndex.get(team1)][teamIndex.get(team2)];
        else throw new IllegalArgumentException();
    }

    public boolean isEliminated(String team)
    { return certificateOfElimination(team) != null; }

    public Iterable<String> certificateOfElimination(String team) {
        if (teamIndex.containsKey(team)) {
            if (!certificates.containsKey(team))
                computeCertificate(team);
            return certificates.get(team);
        } else throw new IllegalArgumentException();
    }

    private void computeCertificate(String team) {
        if (!isTriviallyEliminated(team) && !isNonTriviallyEliminated(team))
            certificates.put(team, null);
    }

    private boolean isTriviallyEliminated(String team) {
        int x = teamIndex.get(team);
        for (int i = 0; i < n; ++i) {
            if (w[x] + r[x] < w[i]) {
                certificates.put(team, Arrays.asList(teams[i]));
                return true;
            }
        }
        return false;
    }

    private boolean isNonTriviallyEliminated(String team) {
        FlowNetwork G = new FlowNetwork((n * n - n >> 1) + 3);
        int x = teamIndex.get(team);
        int s = G.V() - 2;
        int t = G.V() - 1;

        for (int i = 0; i < n; ++i)
            if (i != x)
                G.addEdge(new FlowEdge(i, t, w[x] + r[x] - w[i]));

        for (int i = 0, v = n; i < n; ++i) {
            if (i == x) continue;
            for (int j = i + 1; j < n; ++j) {
                if (j == x) continue;
                G.addEdge(new FlowEdge(s, v, g[i][j]));
                G.addEdge(new FlowEdge(v, i, Double.POSITIVE_INFINITY));
                G.addEdge(new FlowEdge(v, j, Double.POSITIVE_INFINITY));
                ++v;
            }
        }

        FordFulkerson ff = new FordFulkerson(G, s, t);
        for (FlowEdge e : G.adj(s)) {
            if (e.flow() < e.capacity()) {
                List<String> certificate = new ArrayList<>();
                for (int i = 0; i < n; ++i)
                    if (i != x && ff.inCut(i))
                        certificate.add(teams[i]);
                certificates.put(team, certificate);
                return true;
            }
        }
        return false;
    }

}
