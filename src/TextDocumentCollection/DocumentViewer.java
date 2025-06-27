package TextDocumentCollection;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.*;

public class DocumentViewer {
    private DocumentManager documentManager;
    private Scanner scanner;
    private List<Integer> searchResults;
    private int currentSearchIndex;
    private int pageSize = 2000; // В символах
    private final int LINE_LENGTH = 200;
    private static final String OUTPUT_FILE = "output.txt";
    private boolean outputToFile = false;

    public DocumentViewer(DocumentManager documentManager, Scanner scanner) {
        this.documentManager = documentManager;
        this.scanner = scanner;
    }

    public boolean isOutputToFile() {
        return outputToFile;
    }

    public void openDocument() throws IOException {
        documentManager.listDocuments();
        if (documentManager.getDocuments().isEmpty()) return;

        System.out.print("Введите номер документа для открытия: ");
        int docNumber = Integer.parseInt(scanner.nextLine()) - 1;

        Path document = documentManager.getDocument(docNumber);
        System.out.println("Открыт документ: " + document.getFileName());

        String content = Files.readString(document);
        paginateDocument(content, 0);
    }

    private void paginateDocument(String content, int startChar) {
        int totalChars = content.length();
        int endChar = Math.min(startChar + pageSize, totalChars);

        System.out.println("\n--- Страница (символы " + (startChar + 1) + "-" + endChar + " из " + totalChars + ") ---");
        printWithLineLimit(content.substring(startChar, endChar));

        if (totalChars <= pageSize) return;

        while (true) {
            System.out.println("\nКоманды: n - следующая страница, p - предыдущая страница, q - выход");
            System.out.print("Введите команду: ");
            String cmd = scanner.nextLine().trim().toLowerCase();

            if (cmd.equals("n")) {
                if (endChar < totalChars) {
                    paginateDocument(content, endChar);
                } else {
                    System.out.println("Это последняя страница");
                }
                return;
            } else if (cmd.equals("p")) {
                if (startChar > 0) {
                    int newStart = Math.max(0, startChar - pageSize);
                    paginateDocument(content, newStart);
                } else {
                    System.out.println("Это первая страница");
                }
                return;
            } else if (cmd.equals("q")) {
                return;
            } else {
                System.out.println("Неизвестная команда");
            }
        }
    }

    private void printWithLineLimit(String text) {
        Consumer<String> output = outputToFile ?
                line -> {
                    try {
                        Files.write(Paths.get(OUTPUT_FILE), (line + System.lineSeparator()).getBytes(),
                                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                    } catch (IOException e) {
                        System.err.println("Ошибка записи в файл: " + e.getMessage());
                    }
                } :
                System.out::println;

        final int[] currentIndex = {0};

        while (currentIndex[0] < text.length()) {
            int end = Math.min(currentIndex[0] + LINE_LENGTH, text.length());
            String line = text.substring(currentIndex[0], end);

            if (end < text.length() && !Character.isWhitespace(text.charAt(end)) &&
                    !Character.isWhitespace(text.charAt(end - 1))) {
                int lastSpace = line.lastIndexOf(' ');
                if (lastSpace > 0) {
                    end = currentIndex[0] + lastSpace;
                    line = text.substring(currentIndex[0], end);
                }
            }

            output.accept(line);
            currentIndex[0] = end;
        }
    }

    public void searchInDocument() throws IOException {
        Path currentDocument = documentManager.getCurrentDocument();

        System.out.print("Введите текст для поиска: ");
        String searchText = scanner.nextLine();

        String content = Files.readString(currentDocument);

        searchResults = new ArrayList<>();
        int index = content.indexOf(searchText);
        while (index >= 0) {
            searchResults.add(index);
            index = content.indexOf(searchText, index + 1);
        }

        currentSearchIndex = -1;

        if (searchResults.isEmpty()) {
            System.out.println("Текст не найден");
            return;
        }

        System.out.println("Найдено совпадений: " + searchResults.size());
        navigateSearchResults(content, searchText);
    }

    private void navigateSearchResults(String content, String searchText) {
        while (true) {
            System.out.println("\nКоманды: n - следующее совпадение, p - предыдущее совпадение, q - выход");
            System.out.print("Введите команду: ");
            String cmd = scanner.nextLine().trim().toLowerCase();

            if (cmd.equals("n")) {
                if (currentSearchIndex < searchResults.size() - 1) {
                    currentSearchIndex++;
                    showSearchResult(content, searchText, currentSearchIndex);
                } else {
                    System.out.println("Это последнее совпадение");
                }
            } else if (cmd.equals("p")) {
                if (currentSearchIndex > 0) {
                    currentSearchIndex--;
                    showSearchResult(content, searchText, currentSearchIndex);
                } else {
                    System.out.println("Это первое совпадение");
                }
            } else if (cmd.equals("q")) {
                return;
            } else {
                System.out.println("Неизвестная команда");
            }
        }
    }

    private void showSearchResult(String content, String searchText, int resultIndex) {
        int startIndex = searchResults.get(resultIndex);
        int endIndex = startIndex + searchText.length();

        int contextStart = Math.max(0, startIndex - 50);
        int contextEnd = Math.min(content.length(), endIndex + 50);

        String context = content.substring(contextStart, contextEnd);

        String highlighted = context.replace(searchText,
                "\u001B[31m" + searchText + "\u001B[0m");

        System.out.println("\nСовпадение " + (resultIndex + 1) + " из " + searchResults.size() +
                " (позиция " + (startIndex + 1) + "-" + endIndex + "):");
        printWithLineLimit("..." + highlighted + "...");
    }

    public void setPageSize() {
        System.out.print("Введите новый размер страницы (количество символов): ");
        try {
            pageSize = Integer.parseInt(scanner.nextLine());
            if (pageSize <= 0) {
                System.out.println("Размер страницы должен быть положительным числом");
                pageSize = 2000;
            } else {
                System.out.println("Размер страницы установлен: " + pageSize + " символов");
            }
        } catch (NumberFormatException e) {
            System.out.println("Неверный формат числа");
        }
    }

    public void setOutputMode(boolean toFile) {
        this.outputToFile = toFile;
        System.out.println("Режим вывода установлен: " + (toFile ? "в файл " + OUTPUT_FILE : "в консоль"));
    }
}