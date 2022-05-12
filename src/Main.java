import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

public class Main {

    private static final DecimalFormat format = new DecimalFormat("0.000");
    private static final List<double[]> cordsList = new ArrayList<>();
    private static final List<Cluster> clusters = new ArrayList<>();
    private static final List<Point> points = new ArrayList<>();
    private static final List<double[]> clusterCords = new ArrayList<>();

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.print("\u001B[31mInvalid number of parameters");
            System.exit(-7629);
        }

        int k = Integer.parseInt(args[0]);
        readFromFile(System.getProperty("user.dir") + "\\data.txt");
        for (int i = 0; i < k; i++)
            clusters.add(createCluster(String.valueOf(i)));

        for (Cluster cluster : clusters) {
            clusterCords.add(cluster.getCords());
            System.out.println("Cluster \"" + cluster.getGroup() + "\" cords: " + Arrays.toString(cluster.getCords()));
        }
        System.out.println();
        for (int i = 0; i < cordsList.size(); i++) {
            points.add(new Point(cordsList.get(i), clusters.get(findNearestCluster(cordsList.get(i))).getGroup()));
            System.out.println("[Data no. " + (i + 1) + "] Group: " + points.get(i).getGroup() + "; Cords: " +
                    Arrays.toString(points.get(i).getCords()));
        }
        System.out.println();
        int loopCounter = 1;
        do {
            System.out.println("\u001B[31mIteration: " + loopCounter + "\u001B[0m");
            clusters.forEach(e -> {
                e.setCords(newClusterCords(e));
                System.out.println("Cluster \"" + e.getGroup() + "\" cords: " + Arrays.toString(e.getCords()) +
                        "\nAverage distance from point to Cluster \"" + e.getGroup() + "\": " + format.format(distanceFromCluster(e)) + ';');
            });
            System.out.println();
            points.forEach(e -> e.setGroup(clusters.get(findNearestCluster(e.getCords())).getGroup()));
            for (int i = 0; i < points.size(); i++) {
                System.out.println("[Data no. " + (i + 1) + "] Group: " + points.get(i).getGroup() + "; Cords: " +
                        Arrays.toString(points.get(i).getCords()));
            }
            System.out.println();
            loopCounter++;
        }
        while (!changerClusterCords(k));
        System.out.println("\u001B[32mTotal number of iterations: " + --loopCounter);
        clusters.forEach(e -> System.out.println("Group \"" + e.getGroup() + "\" has got " + points.stream()
                .filter(f -> f.getGroup().equals(e.getGroup()))
                .count() + " members and the average distance to point is " + format.format(distanceFromCluster(e))));
    }

    private static void readFromFile(String path) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
            String line;
            while ((line = bufferedReader.readLine()) != null)
                cordsList.add(Arrays
                        .stream(line.split(","))
                        .mapToDouble(Double::parseDouble)
                        .toArray());
        } catch (IOException ignored) {
            System.out.println("Invalid path");
        }
    }

    private static Cluster createCluster(String group) {
        double[] cords = cordsList.get((int) (Math.random() * cordsList.size()));
        Cluster cluster = new Cluster(cords, group);
        for (Cluster tempCluster : clusters) {
            while (tempCluster.getCords() == cluster.getCords()) {
                cords = cordsList.get((int) (Math.random() * cordsList.size()));
                cluster.setCords(cords);
            }
        }
        return cluster;
    }

    private static int findNearestCluster(double[] cords) {
        double distance = 0;
        List<Double> distances = new ArrayList<>();
        for (Cluster cluster : clusters) {
            for (int j = 0; j < cords.length; j++)
                distance += Math.pow(cords[j] - cluster.getCords()[j], 2);
            distances.add(Math.sqrt(distance));
            distance = 0;
        }
        return distances.indexOf(Collections.min(distances));
    }

    public static double[] newClusterCords(Cluster cluster) {
        double[] newCords = new double[cluster.getCords().length];
        double fractionUpper = 0;
        double fractionLower = 0;
        for (int i = 0; i < cluster.getCords().length; i++) {
            for (Point point : points) {
                if (point.getGroup().equals(cluster.getGroup())) {
                    fractionUpper += point.getCords()[i];
                    fractionLower++;
                }
            }
            newCords[i] = Double.parseDouble(format.format(fractionUpper / fractionLower));
            fractionUpper = 0;
            fractionLower = 0;
        }
        clusterCords.add(newCords);
        return newCords;
    }

    private static boolean changerClusterCords(int k) {
        if (k == clusterCords.size())
            return false;
        boolean returner = true;
        int lastIndex = clusterCords.size() - 1;
        for (int i = 0; i < k; i++) {
            if (!Arrays.equals(clusterCords.get(lastIndex - i), clusterCords.get(lastIndex - i - k))) {
                returner = false;
                break;
            }
        }
        return returner;
    }

    private static double distanceFromCluster(Cluster cluster) {
        double[] clusterCords = cluster.getCords();
        String clusterName = cluster.getGroup();
        List<Double> distances = new ArrayList<>();
        points.stream().filter(e -> e.getGroup().equals(clusterName)).forEach(point -> {
            double distance = 0;
            for (int i = 0; i < point.getCords().length; i++)
                distance += Math.pow(clusterCords[i] - point.getCords()[i], 2);
            distances.add(Math.sqrt(distance));
        });
        return distances.stream().mapToDouble(Double::doubleValue).sum() / (double) distances.size();
    }

}


class Point {
    protected double[] cords;
    protected String group;

    public Point(double[] cords, String group) {
        this.cords = cords;
        this.group = group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getGroup() {
        return group;
    }

    public double[] getCords() {
        return cords;
    }


}

class Cluster extends Point {
    public Cluster(double[] cords, String group) {
        super(cords, group);
    }

    public void setCords(double[] cords) {
        this.cords = cords;
    }

    public String getGroup() {
        return group;
    }
}