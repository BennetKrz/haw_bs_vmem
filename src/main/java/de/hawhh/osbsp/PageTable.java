package de.hawhh.osbsp;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

/**
 * Eine Seitentabelle eines Prozesses, implementiert als ArrayList von
 * PageTableEntry-Elementen (pte)
 *
 */
public class PageTable {

    /**
     * Die Seitentabelle als ArrayList von Seitentabelleneinträgen
     * (PageTableEntry). Die Seitentabelle darf nicht sortiert werden!
     */
    private final ArrayList<PageTableEntry> pageTable;

    /**
     * Liste aller Seiten, die sich im RAM befinden
     */
    private final LinkedList<PageTableEntry> pteRAMlist;

    /**
     * Uhrzeiger für Clock-Algorithmus
     */
    private int pteRAMlistIndex;

    /**
     * Zeiger auf das Betriebssystem-Objekt
     */
    private final OperatingSystem os;

    /**
     * Prozess-ID des eigenen Prozesses
     */
    private final int pid;

    /**
     * Konstruktor
     */
    public PageTable(OperatingSystem currentOS, int myPID) {
        os = currentOS;
        pid = myPID;
        // Die Seitentabelle erzeugen
        pageTable = new ArrayList<PageTableEntry>();
        // Die Liste auf RAM-Seiteneinträge erzeugen
        pteRAMlist = new LinkedList<PageTableEntry>();
        pteRAMlistIndex = 0;
    }

    /**
     * Rückgabe: Seitentabelleneintrag pte (PageTableEntry) für die übergebene
     * virtuelle Seitennummer (VPN = Virtual Page Number) oder null, falls Seite
     * nicht existiert
     */
    public PageTableEntry getPte(int vpn) {
        if ((vpn < 0) || (vpn >= pageTable.size())) {
            // Rückgabe null, da Seite nicht existiert!
            return null;
        } else {
            return pageTable.get(vpn);
        }
    }

    /**
     * Einen Eintrag (PageTableEntry) an die Seitentabelle anhängen. Die
     * Seitentabelle darf nicht sortiert werden!
     */
    public void addEntry(PageTableEntry pte) {
        pageTable.add(pte);
    }

    /**
     * R�ckgabe: Aktuelle Größe der Seitentabelle.
     */
    public int getSize() {
        return pageTable.size();
    }

    /**
     * Pte in pteRAMlist eintragen, wenn sich die Zahl der RAM-Seiten des
     * Prozesses erhöht hat.
     */
    public void pteRAMlistInsert(PageTableEntry pte) {
        pteRAMlist.add(pte);
    }

    /**
     * Eine Seite, die sich im RAM befindet, anhand der pteRAMlist auswählen und
     * zurückgeben
     */
    public PageTableEntry selectNextRAMpteAndReplace(PageTableEntry newPte) {
        if (os.getReplacementAlgorithm() == OperatingSystem.ImplementedReplacementAlgorithms.CLOCK) {
            return clockAlgorithm(newPte);
        } else {
            if (os.getReplacementAlgorithm() == OperatingSystem.ImplementedReplacementAlgorithms.FIFO) {
                return fifoAlgorithm(newPte);
            } else {
                return randomAlgorithm(newPte);
            }
        }
    }

    /**
     * FIFO-Algorithmus: Auswahl = Listenkopf (1. Element) Anschließend
     * Listenkopf löschen, neue Seite (newPte) an Liste anhängen
     */
    private PageTableEntry fifoAlgorithm(PageTableEntry newPte) {
        PageTableEntry pte; // Auswahl

        pte = (PageTableEntry) pteRAMlist.getFirst();
        os.testOut("Prozess " + pid + ": FIFO-Algorithmus hat pte ausgewählt: "
                + pte.virtPageNum);
        pteRAMlist.removeFirst();
        pteRAMlist.add(newPte);
        return pte;
    }

    /**
     * CLOCK-Algorithmus (Second-Chance): Nächstes Listenelement, ausgehend vom
     * aktuellen Index, mit Referenced-Bit = 0 (false) auswählen. Sonst R-Bit auf
     * 0 setzen und nächstes Element in der pteRAMlist untersuchen. Anschließend
     * die ausgewählte Seite durch die neue Seite (newPte) am selben Listenplatz
     * ersetzen
     */
    private PageTableEntry clockAlgorithm(PageTableEntry newPte) {
        int i;
        for(i = pteRAMlistIndex; i < pteRAMlist.size() && pteRAMlist.get(i).referenced; i++) {
            PageTableEntry pte = pteRAMlist.get(i);
            pte.referenced = false;

            if(i == pteRAMlist.size() - 1) {
                i = 0;
            }
        }

        PageTableEntry pteFound = pteRAMlist.get(i);
        os.testOut("Prozess " + pid + ": Clock-Algorithmus hat pte ausgewählt: "
                + pteFound.virtPageNum);

        pteRAMlist.remove(pteFound);
        pteRAMlist.add(newPte);

        return pteFound;
    }

    /**
     * RANDOM-Algorithmus: Zufällige Auswahl
     */
    private PageTableEntry randomAlgorithm(PageTableEntry newPte) {
        int ramPteListReplaceIndex = new Random().nextInt(pteRAMlist.size());
        PageTableEntry pte = pteRAMlist.get(ramPteListReplaceIndex);

        os.testOut("Prozess " + pid + ": Random-Algorithmus hat pte ausgewählt: "
                + pte.virtPageNum);

        pteRAMlist.remove(pte);
        pteRAMlist.add(newPte);

        return pte;
    }

    // ----------------------- Hilfsmethode --------------------------------
    /**
     * ramPteIndex zirkular hochzählen zwischen 0 .. Listengröße-1
     */
    private void incrementPteRAMlistIndex() {
        pteRAMlistIndex = (pteRAMlistIndex + 1) % pteRAMlist.size();
    }

}
