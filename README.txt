Пускане на TCP сървър на порт 9191. Всеки потребител се идентифицира по TCP сесията си. Първо трябва да изпрати текста на модела в AMPL формат и да завърши модела с 'MODEL END' (без кавичките) и нов ред.След това изчава да получи 'MODEL RECEIVED' и нов ред за да започне интерактивната сесия със солвъра.Telnet не може да се използва за тестове ако има кирилица в модела. Кодирането е UTF-8.

java -cp target/fiem-1.2.0-SNAPSHOT.jar bg.bas.iit.weboptim.solver.fiem.net.FiemTCPServer


Стартиране на солвъра в конзола с примерния тестови проблем

java -jar target/fiem-1.2.0-SNAPSHOT.jar solve --modelFile src/test/resources/bg/bas/iit/weboptim/solver/fiem/steps/base_problem.mod 
