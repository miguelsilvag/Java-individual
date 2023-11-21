import com.github.britooo.looca.api.core.Looca;
import com.github.britooo.looca.api.group.discos.Disco;
import entities.*;
import repositories.*;
import utils.Util;

import java.util.concurrent.TimeUnit;
import java.util.Scanner;

public class Application {

    private final static ComputerRepository COMPUTER_REPOSITORY = new ComputerRepository(new ConnectionMySql());
    private final static CpuRepository CPU_REPOSITORY = new CpuRepository(new ConnectionMySql());
    private final static DiskRepository DISK_REPOSITORY = new DiskRepository(new ConnectionMySql());
    private final static NetworkRepository NETWORK_REPOSITORY = new NetworkRepository(new ConnectionMySql());
    private final static StatisticsRepository STATISTICS_REPOSITORY = new StatisticsRepository();

    private static Computer computer = new Computer(1);

    public static void main(String[] args) {
        Looca looca = new Looca();
        Scanner scanner = new Scanner(System.in);

        System.out.println("Bem-vindo ao seu aplicativo!");

        while (true) {
            System.out.println("\nMenu:");
            System.out.println("1. Configuração Inicial");
            System.out.println("2. Monitorar Estatísticas");
            System.out.println("3. Sair");
            System.out.print("Escolha uma opção: ");

            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    setup();
                    break;
                case 2:
                    monitorStatistics(looca);
                    break;
                case 3:
                    System.out.println("Encerrando o aplicativo.");
                    scanner.close();
                    System.exit(0);
                default:
                    System.out.println("Opção inválida. Por favor, escolha uma opção válida.");
            }
        }
    }

    public static void monitorStatistics(Looca looca) {
        Statistics statistics = new Statistics();
        Util.setInterval(() -> {
            statistics.setComputer(computer);
            statistics.setTemperature(looca.getTemperatura().getTemperatura());
            statistics.setCpuUsage(looca.getProcessador().getUso());
            statistics.setRamUsage(looca.getMemoria().getEmUso().doubleValue());
            statistics.setRamAvailable(looca.getMemoria().getDisponivel().doubleValue());
            statistics.setRamTotal(looca.getMemoria().getTotal().doubleValue());
            statistics.setDiskTotal(looca.getGrupoDeDiscos().getTamanhoTotal().doubleValue());
            statistics.setDiskUsage(looca.getGrupoDeDiscos().getTamanhoTotal().doubleValue());

            STATISTICS_REPOSITORY.save(statistics);
        }, 2, TimeUnit.SECONDS);
    }

    public static void setup() {
        Looca looca = new Looca();
        Computer com2puter = new Computer();
        Cpu cpu = new Cpu(looca.getProcessador().getId(), looca.getProcessador().getNome());
        Disk disk = new Disk();
        for (Disco d : looca.getGrupoDeDiscos().getDiscos()) {
            disk.setModel(d.getModelo());
            disk.setId(d.getSerial());
        }
        computer.setHostname(looca.getRede().getParametros().getHostName());
        computer.setSystemInfo(looca.getSistema().getSistemaOperacional());
        computer.setMaker(looca.getSistema().getFabricante());
        computer.setDisk(disk);
        computer.setCpu(cpu);

        DISK_REPOSITORY.save(disk);
        CPU_REPOSITORY.save(cpu);
        COMPUTER_REPOSITORY.save(computer);

        Application.computer = computer;
        Application.computer.setId(1);
        Network network = new Network(Application.computer, looca.getRede().getGrupoDeInterfaces().getInterfaces().get(0));

        NETWORK_REPOSITORY.save(network);

        System.out.println("Configuração inicial concluída com sucesso!");
    }
}
