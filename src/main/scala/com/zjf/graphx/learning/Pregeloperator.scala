package com.zjf.graphx.learning

import org.apache.spark.graphx.{Graph, VertexId}
import org.apache.spark.graphx.util.GraphGenerators
import org.apache.spark.{SparkConf, SparkContext}

/**
  * 使用pregel函数求单源最短路径
  */
object Pregeloperator {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("CollectingNeighbors").setMaster("local[4]")
    // Assume the SparkContext has already been constructed
    val sc = new SparkContext(conf)
    // A graph with edge attributes containing distances
    val graph: Graph[Long, Double] =
      GraphGenerators.logNormalGraph(sc, numVertices = 5).mapEdges(e => e.attr.toDouble)
    val sourceId: VertexId = 2 // The ultimate source
    // Initialize the graph such that all vertices except the root have distance infinity.

    println("graph:");
    println("vertices:");
    graph.vertices.collect.foreach(println)
    println("edges:");
    graph.edges.collect.foreach(println)
    println();

    val initialGraph = graph.mapVertices((id, _) => if (id == sourceId) 0.0 else Double.PositiveInfinity)
    println("initialGraph:");
    println("vertices:");
    initialGraph.vertices.collect.foreach(println)
    println("edges:");
    initialGraph.edges.collect.foreach(println)
    val sssp = initialGraph.pregel(Double.PositiveInfinity)(
      (id, dist, newDist) => math.min(dist, newDist), // Vertex Program
      triplet => { // Send Message
        if (triplet.srcAttr + triplet.attr < triplet.dstAttr) {
          Iterator((triplet.dstId, triplet.srcAttr + triplet.attr))
        } else {
          Iterator.empty
        }
      },
      (a, b) => math.min(a, b) // Merge Message
    )
    println();
    println("sssp:");
    println("vertices:");
    println(sssp.vertices.collect.mkString("\n"))
    println("edges:");
    sssp.edges.collect.foreach(println)
  }
}
