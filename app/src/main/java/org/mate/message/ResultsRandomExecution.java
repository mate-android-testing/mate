package org.mate.message;

import org.mate.model.graph.EventEdge;
import org.mate.model.graph.ScreenNode;
import org.mate.state.IScreenState;
import org.mate.ui.WidgetAction;
import org.mate.utils.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ResultsRandomExecution {
    private static Map<String, List<WidgetAction>> sortMap(Map<String, List<WidgetAction>> historic){
        Map<String, List<WidgetAction>> sortedMap = new TreeMap<>(historic);
        return  sortedMap;
    }

   /* @SuppressLint("NewApi")
    private static Map<String, List<WidgetAction>> sortMap2(Map<String, List<WidgetAction>> historic){
        LinkedHashMap<String, List<WidgetAction>> sortedMap = new LinkedHashMap<>();

        historic.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .forEachOrdered(x -> sortedMap.put(x.getKey(), x.getValue()));
        return sortedMap;
    }*/

    /**
     * TODO - ONE MORE FUCKING TIME
     * @param historic
     */
   public static void printResultsNew(Map<String, ScreenNode>  historic){
       System.out.println("");
       System.out.println("-------------------------------Resultados---------------------------------");
       for (Map.Entry<String, ScreenNode> i : historic.entrySet()) {
           System.out.println("Screen: " + i.getKey() + " | QTDE de ações: " + i.getValue().getEventEdges().size());
           System.out.println("Acções executadas:");
           for (EventEdge widget : i.getValue().getEventEdges()) {
               if (widget.getWidgetAction().isExecuted()) {
                   System.out.println("> AÇÃO: " + widget.getWidgetAction().getActionType());
                   System.out.println("  -> Weight - " + widget.getWeight());
                   System.out.println("  -> Fitness - " + widget.getFitness());
                   System.out.println("  -> Executado? " + widget.getWidgetAction().isExecuted() + "(x" + widget.getWidgetAction().getQtdeOfExec() + ")");
                   if (widget.isNewStateGenerated()) {
                       System.out.println("    --> Gerou novo estado - " + widget.getTarget().getId());
                   }
               }
           }
       }
   }
    public static void printResults(Map<String, List<WidgetAction>> historic){
        //historic = sortMap(historic);
        System.out.println("");
        System.out.println("-------------------------------Resultados---------------------------------");
        for (Map.Entry<String, List<WidgetAction>> i : historic.entrySet()) {
            System.out.println("Screen: " + i.getKey() + " | QTDE de ações: " + i.getValue().size());
            System.out.println("Acções executadas:");
            for (WidgetAction widget : i.getValue()) {
                if (widget.isExecuted()) {
                    System.out.println("> AÇÃO: " + widget.getActionType());
                    System.out.println("  -> Weight - " + widget.getWeight());
                    System.out.println("  -> Fitness - " + widget.getFitness());
                    System.out.println("  -> Executado? " + widget.isExecuted() + "(x" + widget.getQtdeOfExec() + ")");
                    if (widget.isNewStateGenerated()) {
                        System.out.println("    --> Gerou novo estado - " + widget.getAdjScreen().getId());
                    }
                }
            }
        }
    }
    public static void printResults_older(Map<IScreenState, List<WidgetAction>> mapHistoricActionsOfScreenState) {
        System.out.println("");
        System.out.println("-------------------------------Resultados---------------------------------");
        for (Map.Entry<IScreenState, List<WidgetAction>> i : mapHistoricActionsOfScreenState.entrySet()) {
            System.out.println("Screen: " + i.getKey().getActivityName() + " | QTDE de ações: ");
            for (WidgetAction widget : i.getValue()) {
                System.out.println("> Ação: " + widget.getActionType());
                System.out.println("  -> Weight - " + widget.getWeight());
                System.out.println("  -> Fitness - " + widget.getFitness());
                System.out.println("  -> Executado? " + widget.isExecuted());
                if (widget.isExecuted()) {
                    System.out.println("  -> Qtde Execuções: - " + widget.getQtdeOfExec());
                }
                System.out.println("  -> Acionou novo estado? " + widget.isNewStateGenerated());
                if (widget.isNewStateGenerated()) {
                    System.out.println("    --> Estado encontrado - " + widget.getAdjScreen().getActivityName());
                }

            }
        }
    }

    /**
     * Imprime a relação de todas telas geradas no modelo Mock para testar o algoritmo estatico
     * @param listScreensMock
     */
    public static void printScreens(List<IScreenState> listScreensMock) {
        System.out.println("Lista de telas geradas para o teste");
        System.out.println("SCREEN              | Ações possiveis para esta tela (com seus pesos)");
        for (IScreenState i : listScreensMock) {
            /* printSequenceSecreenAccessed(i); */
            System.out.println(i.getActivityName() + (i.getActivityName().length() <= 18 ? "  | " : " | ")
                    + sprintListOfActions(i.getActions()));

        }
    }

    public static void printResults(List<IScreenState> listHistoricActionsOfScreenState) {
        System.out.println("");
        System.out.println("-------------------------------Resultados---------------------------------");
        /*
         * for (IScreenState i : listHistoricActionsOfScreenState) {
         * System.out.println("SCREEN: " + i.getActivityName() + " | QTDE de ações: " +
         * i.getActions().size()); for (WidgetAction widget : i.getActions()) {
         * System.out.println("> Ação: " + widget.getActionType());
         *
         * System.out.println("  -> Weight - " + widget.getWeight());
         * System.out.println("  -> Fitness - " + widget.getFitness());
         * System.out.println("  -> Executado? " + widget.isExecuted()); if
         * (widget.isExecuted()) { System.out.println("  --> Qtde Execuções: - " +
         * widget.getQtdeOfExec()); } System.out.println("  -> Acionou novo estado? " +
         * widget.isNewStateGenerated()); if (widget.isNewStateGenerated()) {
         * System.out.println("    --> Estado encontrado - " +
         * widget.getAdjScreen().getActivityName()); } }
         * System.out.println("**************************************************"); }
         */

        System.out.println("SCREEN              | Telas acessadas");
        for (IScreenState i : listHistoricActionsOfScreenState) {
            /* printSequenceSecreenAccessed(i); */
            System.out.println(i.getId() + ") \n"
                    + printActionsOfScreen(i.getActions()));

            System.out.println("***");
        }
        /*
         * System.out.println("Telas que não geraram novos estados");
         * System.out.println("SCREEN              | Telas acessadas"); for
         * (IScreenState i : listHistoricActionsOfScreenState) {
         * System.out.println(i.getActivityName() + (i.getActivityName().length() <= 18
         * ? "  | " : " | ") + sprintAdjScreens(i.getActions()));
         *
         * }
         */
    }

    public static String printActionsOfScreen(List<WidgetAction> actions) {
        String telas = "";
        for (WidgetAction widget : actions) {
			/*if (widget.isNewStateGenerated()) {
				telas = telas + "[" + widget.getActionType() + "(x" + widget.getQtdeOfExec() + ")]"
						+ widget.getAdjScreen().getActivityName() + ", ";
			}*/
            if (widget.getQtdeOfExec() > 0) {
                telas = telas + "   - [" + widget.getActionType() + "(x" + widget.getQtdeOfExec() + ")]"
                        + (widget.isNewStateGenerated() ? sprintAdjScreens(widget.getAdjScreen()) : "{n/a}") + "\n";
            }
        }
        //telas = StringUtils.isNotBlank(telas) ? telas.substring(0, telas.length() - 2) : telas;
        telas = telas + "\n QTDE de ações: "+ actions.size();
        return telas;
    }

    /**
     * Imprime as telas (estados) encontradas após a execução de uma ação disponivel para esta tela
     * Esta versão irá ser alterada após nova implementação do MATE
     * @param screens
     * @return
     */
    public static String sprintAdjScreens(List<IScreenState> screens) {
        String telas = "";
        for (IScreenState screen : screens) {
            telas = telas + screen.getId()+ ", ";
        }
        if(StringUtils.isNotBlank(telas)){
            telas = "{" + telas.substring(0, telas.length() - 2) + "}";
        }
        telas = telas + " QTDE de telas que foram acessadas: "+ screens.size();
        return telas;
    }

    private static String sprintAdjScreens(IScreenState screen) {
        return screen != null ? screen.getId() : "(n/a)";
    }

    public static String sprintListOfActions(List<WidgetAction> actions) {
        String telas = "";
        for (WidgetAction widget : actions) {
            if (true) {
                //if (widget.isNewStateGenerated()) {
                telas = telas + widget.getActionType() + " (p-" + widget.getWeight() + "), ";
            }
        }
        telas = StringUtils.isNotBlank(telas) ? telas.substring(0, telas.length() - 2) : telas;

        return telas;
    }


    public static void printLogs(List<String> logs) {
        System.out.println(logs);
    }

}
