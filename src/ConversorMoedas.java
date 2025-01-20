import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.InputMismatchException;
import java.util.Locale;
import java.util.Scanner;

public class ConversorMoedas {

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            Gson gson = new GsonBuilder()
                    .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                    .setPrettyPrinting()
                    .create();

            String apiUrl = "https://v6.exchangerate-api.com/v6/c110e1ee2358fd497c96fe61/latest/USD";

            Moeda moeda = obterTaxasDeCambio(apiUrl, gson);
            if (moeda == null) {
                System.out.println("Erro ao obter taxas de câmbio. Encerrando o programa.");
                return;
            }

            DecimalFormat decimalFormat = criarFormatoDecimal();
            int opcao;

            do {
                exibirMenu();
                opcao = scanner.nextInt();
                processarOpcao(opcao, scanner, moeda, decimalFormat);
            } while (opcao != 7);

            System.out.println("Programa encerrado!");
        } catch (IOException | InterruptedException e) {
            System.out.println("Ocorreu um erro ao executar o programa: " + e.getMessage());
        }
    }

    private static Moeda obterTaxasDeCambio(String apiUrl, Gson gson) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(apiUrl)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);

        if (!jsonResponse.has("conversion_rates")) {
            throw new IllegalStateException("O JSON de resposta não contém 'conversion_rates'");
        }

        JsonObject conversionRates = jsonResponse.getAsJsonObject("conversion_rates");

        return gson.fromJson(conversionRates.toString(), Moeda.class);
    }

    private static DecimalFormat criarFormatoDecimal() {
        DecimalFormatSymbols dfs = new DecimalFormatSymbols(Locale.ENGLISH);
        return new DecimalFormat("0.00000000000000", dfs);
    }

    private static void exibirMenu() {
        System.out.println("\n***********************************************************");
        System.out.println("Seja bem-vindo(a) ao Conversor de Moedas!");
        System.out.println("1) Dólar => Peso argentino");
        System.out.println("2) Peso argentino => Dólar");
        System.out.println("3) Dólar => Real brasileiro");
        System.out.println("4) Real brasileiro => Dólar");
        System.out.println("5) Dólar => Peso colombiano");
        System.out.println("6) Peso colombiano => Dólar");
        System.out.println("7) Sair");
        System.out.print("Escolha uma opção: ");
    }

    private static void processarOpcao(int opcao, Scanner scanner, Moeda moeda, DecimalFormat decimalFormat) {
        try {
            switch (opcao) {
                case 1 -> converterMoeda(scanner, moeda.ARS(), "USD", "ARS", decimalFormat);
                case 2 -> converterMoedaInversa(scanner, moeda.ARS(), "ARS", "USD", decimalFormat);
                case 3 -> converterMoeda(scanner, moeda.BRL(), "USD", "BRL", decimalFormat);
                case 4 -> converterMoedaInversa(scanner, moeda.BRL(), "BRL", "USD", decimalFormat);
                case 5 -> converterMoeda(scanner, moeda.COP(), "USD", "COP", decimalFormat);
                case 6 -> converterMoedaInversa(scanner, moeda.COP(), "COP", "USD", decimalFormat);
                case 7 -> System.out.println("Saindo do programa...");
                default -> System.out.println("Opção inválida. Tente novamente.");
            }
        } catch (InputMismatchException e) {
            System.out.println("Entrada inválida. Digite um número válido.");
            scanner.next(); // Limpar a entrada inválida
        }
    }

    private static void converterMoeda(Scanner scanner, double taxaConversao, String moedaOrigem, String moedaDestino, DecimalFormat decimalFormat) {
        try {
            System.out.print("Digite o valor em " + moedaOrigem + ": ");
            double quantidade = scanner.nextDouble();
            double resultado = quantidade * taxaConversao;
            System.out.println("Valor convertido: " + decimalFormat.format(resultado) + " " + moedaDestino);
        } catch (InputMismatchException e) {
            System.out.println("Entrada inválida. Digite um número válido.");
            scanner.next(); // Limpar a entrada inválida
        }
    }

    private static void converterMoedaInversa(Scanner scanner, double taxaConversao, String moedaOrigem, String moedaDestino, DecimalFormat decimalFormat) {
        try {
            System.out.print("Digite o valor em " + moedaOrigem + ": ");
            double quantidade = scanner.nextDouble();
            double resultado = quantidade / taxaConversao;
            System.out.println("Valor convertido: " + decimalFormat.format(resultado) + " " + moedaDestino);
        } catch (InputMismatchException e) {
            System.out.println("Entrada inválida. Digite um número válido.");
            scanner.next(); // Limpar a entrada inválida
        }
    }
}
