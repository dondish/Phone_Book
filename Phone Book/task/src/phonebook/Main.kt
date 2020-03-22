package phonebook

import java.io.FileInputStream
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.math.abs

data class Contact(val name: String, val number: String) {
    operator fun compareTo(contact: Contact): Int {
        return name.compareTo(contact.name)
    }

    override fun toString(): String {
        return "$name: $number"
    }
    companion object {
        fun fromLine(line: String): Contact {
            val splut = line.split(" ")
            return Contact( splut.subList(1, splut.size).joinToString(" "), splut[0])
        }
    }
}

// Using linear probing
class HashTable {
    var length = 0
        private set
    var size = 0
        private set
    private var table: Array<Contact?> = Array(0) {null}

    private fun rebuildTable() {
        if (size == 0) {
            size = 1
            table = Array(1) {null}
            return
        }
        size *= 2
        val oldtable = table
        table = Array(size) { null }
        for (c in oldtable) {
            if (c != null) {
                insert(c)
            }
        }
//        println("Load Factor Exceeded! Current Length: $length, Old Table Size: ${size/2}, New Table Size: $size")
    }

    private fun h(x: String): Int {
        return abs(x.hashCode()) %size
    }

    fun search(name: String): Contact? {
        var i = h(name)
        while (table[i] != null && table[i]?.name != name) {
            i = (i + 1)%size
        }
        return table[i]
    }

    fun insert(contact: Contact) {
        if (size == 0 || ((length+1).toDouble() / size.toDouble() > 0.5) ) {
            rebuildTable()
        }
        var i = h(contact.name)
        while (table[i] != null && table[i]?.name != contact.name) {
            i = (i + 1)%size
        }
        if (table[i] == null) length++
        table[i] = contact
    }

}

fun timeFromMillis(ms: Long): String {
    var secs = ms / 1000
    val mils = ms % 1000
    val mins =  secs / 60
    secs %= 60
    return "$mins min. $secs sec. $mils ms."

}

fun bubbleSort(contacts: MutableList<Contact>, linearSearchTime: Long): Long {
    var n = contacts.size
    val start = System.currentTimeMillis()
    while (n > 0) {
        for (i in 0 until (n-1)) {
            if (System.currentTimeMillis() - start > 10*linearSearchTime) return System.currentTimeMillis() - start
            if (contacts[i] > contacts[i+1]) {
                val temp = contacts[i]
                contacts[i] = contacts[i+1]
                contacts[i+1] = temp
            }
        }
        n--
    }
    return 0
}

fun jumpSearch(contacts: List<Contact>, name: String): Int {
    val n = contacts.size
    var a = 0
    var b = sqrt(n.toDouble()).toInt()
    while (contacts[min(b,n)-1].name < name) {
        a = b
        b += sqrt(n.toDouble()).toInt()
        if (a >= n) return -1
    }
    while (contacts[a].name < name) {
        a++
        if (a == min(b, n)) return -1
    }
    return if (contacts[a].name == name) {
        a
    } else {
        -1
    }
}

fun swap(contacts: MutableList<Contact>, i: Int, j: Int) {
    val temp = contacts[i]
    contacts[i] = contacts[j]
    contacts[j] = temp
}

// Hoare's Partition
fun qsortPartition(contacts: MutableList<Contact>, lo: Int, hi: Int): Int {
    val pivot = contacts[(lo+hi) / 2]
    var i = lo - 1
    var j = hi + 1
    while (true) {
        do {
            i++
        } while (contacts[i] < pivot)
        do {
            j--
        } while (contacts[j] > pivot)
        if (i>=j) return j
        swap(contacts, i, j)
    }
}

fun quickSort(contacts: MutableList<Contact>, lo: Int, hi: Int) {
    if (lo < hi) {
        val p = qsortPartition(contacts, lo, hi)
        quickSort(contacts, lo, p)
        quickSort(contacts, p+1, hi)
    }
}

fun quickSort(contacts: MutableList<Contact>) {
    quickSort(contacts, 0, contacts.size-1)
}

fun binarySearch(contacts: MutableList<Contact>, contact: String): Int {
    var lo = 0
    var hi = contacts.size
    while (lo < hi - 1) {
        val mid = (lo+hi)/2
        when {
            contacts[mid].name < contact -> {
                lo = mid
            }
            contacts[mid].name > contact -> {
                hi = mid
            }
            else -> {
                return mid
            }
        }
    }
    return if (contacts[lo].name == contact) {
        lo
    } else {
        -1
    }
}

fun main() {
    println("Start searching... (linear search)" )
    var startTime = System.currentTimeMillis()
    val contactsStream = FileInputStream("C:\\Users\\odeds\\Downloads\\directory.txt")
    val findStream = FileInputStream("C:\\Users\\odeds\\Downloads\\find.txt")
    val contacts = contactsStream.reader().readLines().map { l -> Contact.fromLine(l) }.toMutableList()
    val names = findStream.reader().readLines()
    var entries = names.mapNotNull { contacts.find { contact -> contact.name == it } }
    var total =  System.currentTimeMillis() - startTime
    println("Found ${entries.size} / ${names.size} entries. Time taken: ${timeFromMillis(total)}")

    println()
    println("Start searching... (bubble sort + jump search)" )
    startTime = System.currentTimeMillis()

    var sortingTime = bubbleSort(contacts, total)
    if (sortingTime == 0L) {
        sortingTime = System.currentTimeMillis() - startTime
        entries = names.map { jumpSearch(contacts, it) }.filter { it >= 0 }.map { contacts[it] }
        total =  System.currentTimeMillis() - startTime
        val searchingTime = System.currentTimeMillis() - sortingTime - startTime
        println("Found ${entries.size} / ${names.size} entries. Time taken: ${timeFromMillis(total)}")
        println("Sorting time: ${timeFromMillis(sortingTime)}")
        println("Searching time: ${timeFromMillis(searchingTime)}")
    } else {
        entries = names.mapNotNull { contacts.find { contact -> contact.name == it } }
        total =  System.currentTimeMillis() - startTime
        val searchingTime = System.currentTimeMillis() - sortingTime - startTime
        println("Found ${entries.size} / ${names.size} entries. Time taken: ${timeFromMillis(total)}")
        println("Sorting time: ${timeFromMillis(sortingTime)} - STOPPED, moved to linear search")
        println("Searching time: ${timeFromMillis(searchingTime)}")
    }

    println()
    println("Start searching... (quick sort + binary search)" )
    startTime = System.currentTimeMillis()

    quickSort(contacts)
    sortingTime = System.currentTimeMillis() - startTime
    entries = names.map { binarySearch(contacts, it) }.filter { it >= 0 }.map { contacts[it] }
    total =  System.currentTimeMillis() - startTime
    val searchingTime = System.currentTimeMillis() - sortingTime - startTime
    println("Found ${entries.size} / ${names.size} entries. Time taken: ${timeFromMillis(total)}")
    println("Sorting time: ${timeFromMillis(sortingTime)}")
    println("Searching time: ${timeFromMillis(searchingTime)}")


    println()
    println("Start searching... (hash table)" )
    startTime = System.currentTimeMillis()
    val table = HashTable()

    for (contact in contacts) {
        table.insert(contact)
    }
    val creatingTime = System.currentTimeMillis() - startTime
    entries = names.mapNotNull { table.search(it) }
    total = System.currentTimeMillis() - startTime
    val lookupTime = System.currentTimeMillis() - creatingTime - startTime
    println("Found ${entries.size} / ${names.size} entries. Time taken: ${timeFromMillis(total)}")
    println("Creating time: ${timeFromMillis(creatingTime)}")
    println("Searching time: ${timeFromMillis(lookupTime)}")

//    println("Start searching...\n" +
//            "Found 500 / 500 entries. Time taken: 0 min. 2 sec. 303 ms.")
}
