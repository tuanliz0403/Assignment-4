import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

public class TestPart6 {

    private<L> CombinedPolicy<String, L> makeCombinedPolicy() {
        Collection<TraversalPolicy<String, L>> list = new ArrayList<>();
        list.add(new DoorPolicy<>());
        list.add(new TerrainPenaltyPolicy<>());
        return new CombinedPolicy<>(list);
    }

    @Test
    void testEdgeHasOnlyDoor() {
        DungeonGraph<String, Object> g = new DungeonGraph<>();
        Vertex<String, Object> A = g.addVertex("A");
        Vertex<String, Object> B = g.addVertex("B");

        Edge<String, Object> e = g.addEdge(A, B, 5, Door.makeDoor("key1"));
        CombinedPolicy<String, Object> policy = makeCombinedPolicy();

        Inventory inv1 = new Inventory();
        assertFalse(policy.traversable(e, inv1), "Can not traverse door without key");

        inv1.add("key1");
        assertTrue(policy.traversable(e, inv1), "Can traverse door when correct key is present");

        assertEquals(5, policy.weight(e, inv1), "Door does not change weight");
    }

    @Test
    void testEdgeHasOnlyTerrain() {
        DungeonGraph<String, Object> g = new DungeonGraph<>();
        Vertex<String, Object> A = g.addVertex("A");
        Vertex<String, Object> B = g.addVertex("B");

        Edge<String, Object> e = g.addEdge(A, B, 3, TerrainFactory.makeTerrain("Swamp"));
        CombinedPolicy<String, Object> policy = makeCombinedPolicy();

        Inventory inv = new Inventory();
        assertTrue(policy.traversable(e, inv), "Terrain should always be traversable");

        assertEquals(5, policy.weight(e, inv), "Swamp adds 2 penalty without SwampBoots");

        inv.add("SwampBoots");
        assertEquals(3, policy.weight(e, inv), "Swamp penalty avoided with correct item");
    }

    @Test
    void testEdgeHasNeitherDoorNorTerrain() {
        DungeonGraph<String, Object> g = new DungeonGraph<>();
        Vertex<String, Object> A = g.addVertex("A");
        Vertex<String, Object> B = g.addVertex("B");

        Edge<String, Object> e = g.addEdge(A, B, 4, "Normal");
        CombinedPolicy<String, Object> policy = makeCombinedPolicy();
        Inventory inv = new Inventory();

        assertTrue(policy.traversable(e, inv), "Normal edge should always be traversable");
        assertEquals(4, policy.weight(e, inv), "Normal adds no penalty");
    }

    @Test
    void testPathSolvableAfterPickingUpKey() {
        DungeonGraph<String, Object> g = new DungeonGraph<>();

        Vertex<String, Object> A = g.addVertex("A");
        Vertex<String, Object> B = g.addVertex("B", "FireKey");
        Vertex<String, Object> C = g.addVertex("C");
        Vertex<String, Object> D = g.addVertex("D");

        g.addEdge(A, B, 2, TerrainFactory.makeTerrain("Normal"));
        g.addEdge(B, C, 3, Door.makeDoor("FireKey"));
        g.addEdge(C, D, 2, TerrainFactory.makeTerrain("Normal"));

        CombinedPolicy<String, Object> policy = makeCombinedPolicy();
        Inventory inv = new Inventory();

        boolean solvable = Path.isQuestSolvable(g, A, D, inv);
        assertTrue(solvable, "Quest should be solvable after picking up FireKey at B");
    }

    @Test
    void testUnsolvableDueToUnreachableKey() {
        DungeonGraph<String, Object> g = new DungeonGraph<>();

        Vertex<String, Object> A = g.addVertex("A");
        Vertex<String, Object> B = g.addVertex("B");
        Vertex<String, Object> C = g.addVertex("C", "FireKey");
        Vertex<String, Object> D = g.addVertex("D");

        g.addEdge(A, B, 2, Door.makeDoor("FireKey"));
        g.addEdge(B, C, 3, TerrainFactory.makeTerrain("Normal"));
        g.addEdge(C, D, 2, TerrainFactory.makeTerrain("Normal"));

        CombinedPolicy<String, Object> policy = makeCombinedPolicy();
        Inventory inv = new Inventory();

        boolean solvable = Path.isQuestSolvable(g, A, D, inv);
        assertFalse(solvable, "Unsolvable since FireKey is behind its own locked door");
    }

    @Test
    void testSolvableWithMixedEdges() {
        DungeonGraph<String, Object> g = new DungeonGraph<>();

        Vertex<String, Object> A = g.addVertex("A");
        Vertex<String, Object> B = g.addVertex("B", "SwampBoots");
        Vertex<String, Object> C = g.addVertex("C");
        Vertex<String, Object> D = g.addVertex("D");

        g.addEdge(A, B, 2, TerrainFactory.makeTerrain("Swamp"));
        g.addEdge(B, C, 3, Door.makeDoor("FireKey"));
        g.addEdge(C, D, 2, TerrainFactory.makeTerrain("Normal"));

        CombinedPolicy<String, Object> policy = makeCombinedPolicy();
        Inventory inv = new Inventory();
        inv.add("FireKey");

        boolean solvable = Path.isQuestSolvable(g, A, D, inv);
        assertTrue(solvable, "Should be solvable after picking up SwampBoots and using FireKey");
    }
}
