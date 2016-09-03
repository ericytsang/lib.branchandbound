package com.github.ericytsang.lib.branchandbound

// todo: encapsulate all this stuff into a singleton object
// todo: add test cases for this stuff
fun <Node> branchAndBound(initialNode:Node,branch:(Node)->Set<Node>,bounds:(Node)->Bounds,checkSolution:(Node)->Boolean):Node?
{
    return branchAndBound(mutableMapOf(initialNode to rootNodeMetaData),branch,bounds,checkSolution)
}

/**
 * the branch and bound algorithm...
 *
 * the [candidateNodes] parameter is a map of [Node]s to [Double]s. the
 * [Double] value represents that [Node]'s potential to be the solution. the
 * [Node]s in this collection will be passed to the supplied [branch] function.
 *
 * the [branch] function takes a [Node], and returns a map of [Node]s to
 * [Double]s. the [Double] value represents that [Node]'s upper bound.
 *
 * the [isSolution] function is passed a [Node], and should return true if
 * the [Node] is the solution, and false otherwise.
 *
 * the function will return the first [Node] that [isSolution] returns true
 * for. if [isSolution] returns for all [Node]s, then the function will
 * return null.
 */
fun <Node> branchAndBound(candidateNodes:MutableMap<Node,NodeMetaData>,branch:(Node)->Set<Node>,bounds:(Node)->Bounds,isSolution:(Node)->Boolean):Node?
{
    val unbranchedNodes = fun():Set<Node> = candidateNodes.keys
    val metadata = fun(node:Node):NodeMetaData = candidateNodes[node] ?: rootNodeMetaData
    var bestCandidate:Node

    // until we find the solution...or exhaust all options
    do
    {
        // update the best candidate reference
        bestCandidate = unbranchedNodes()
            // consider all the nodes with the best upper bounds
            .groupBy {metadata(it).bounds.upperBound}.maxBy {it.key}?.value
            // of those, consider all the nodes with the best lower bounds
            ?.groupBy {metadata(it).bounds.lowerBound}?.maxBy {it.key}?.value
            // of those, select the one with the most depth
            ?.maxBy {metadata(it).depth} ?: return null

        // branch the best candidate and update the candidate nodes map
        val bestCandidateMetaData = metadata(bestCandidate)
        val newNodes = branch(bestCandidate)
        candidateNodes.remove(bestCandidate)
        candidateNodes.putAll(newNodes.map {it to NodeMetaData(bounds(it),bestCandidateMetaData.depth+1)})
    }
    while (!isSolution(bestCandidate))

    return bestCandidate
}

val rootNodeMetaData = NodeMetaData(Bounds(0.0,0.0),0)

class Bounds(val upperBound:Double,val lowerBound:Double)

class NodeMetaData(val bounds:Bounds,val depth:Int)
