package com.example.medicalhomevisit;

import com.example.medicalhomevisit.models.entities.*;
import com.example.medicalhomevisit.models.enums.UserRole;
import com.example.medicalhomevisit.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final MedicalPersonRepository medicalPersonRepository;
    private final PasswordEncoder passwordEncoder;
    private final PatientRepository patientRepository;
    private final ProtocolTemplateRepository protocolTemplateRepository; // Добавляем репозиторий для шаблонов

    @Autowired
    public DataInitializer(RoleRepository roleRepository,
                           UserRepository userRepository,
                           MedicalPersonRepository medicalPersonRepository,
                           PasswordEncoder passwordEncoder,
                           PatientRepository patientRepository,
                           ProtocolTemplateRepository protocolTemplateRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.medicalPersonRepository = medicalPersonRepository;
        this.passwordEncoder = passwordEncoder;
        this.patientRepository = patientRepository;
        this.protocolTemplateRepository = protocolTemplateRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("DataInitializer: Starting data initialization...");

        // 1. Создаем роли, если их нет
        for (UserRole userRoleEnum : UserRole.values()) {
            if (roleRepository.findByName(userRoleEnum).isEmpty()) {
                Role role = new Role();
                role.setName(userRoleEnum);
                roleRepository.save(role);
                log.info("DataInitializer: Created role: {}", userRoleEnum.name());
            }
        }

        // 2. Создаем тестового администратора, если его еще нет
        String adminEmail = "admin@example.com";
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            log.info("DataInitializer: Creating test admin with email: {}", adminEmail);

            Role adminRole = roleRepository.findByName(UserRole.ADMIN)
                    .orElseThrow(() -> new RuntimeException("Error: ADMIN role not found."));

            UserEntity adminUser = new UserEntity();
            adminUser.setEmail(adminEmail);
            adminUser.setFullName("Главный Администратор");
            adminUser.setPassword(passwordEncoder.encode("admin123")); // Установи надежный пароль
            adminUser.setRole(adminRole);
            adminUser.setActive(true);

            userRepository.save(adminUser);
            log.info("DataInitializer: Test admin {} created successfully with ID: {}", adminEmail, adminUser.getId());
        } else {
            log.info("DataInitializer: Test admin with email {} already exists.", adminEmail);
        }

        // 3. Создаем тестового медицинского работника, если его еще нет
        String medicalStaffEmail = "doctor.test@example.com";
        if (userRepository.findByEmail(medicalStaffEmail).isEmpty()) {
            log.info("DataInitializer: Creating test medical staff with email: {}", medicalStaffEmail);

            Role medicalStaffRole = roleRepository.findByName(UserRole.MEDICAL_STAFF)
                    .orElseThrow(() -> new RuntimeException("Error: MEDICAL_STAFF role not found."));

            UserEntity medicalStaffUser = new UserEntity();
            medicalStaffUser.setEmail(medicalStaffEmail);
            medicalStaffUser.setFullName("Доктор Тестов Тест Тестович");
            medicalStaffUser.setPassword(passwordEncoder.encode("password123"));
            medicalStaffUser.setRole(medicalStaffRole);
            medicalStaffUser.setActive(true);
            UserEntity savedUser = userRepository.save(medicalStaffUser);

            MedicalPerson medicalPerson = new MedicalPerson();
            medicalPerson.setUser(savedUser);
            medicalPerson.setSpecialization("Терапевт");
            medicalPersonRepository.save(medicalPerson);

            log.info("DataInitializer: Test medical staff {} created successfully.", medicalStaffEmail);
        } else {
            log.info("DataInitializer: Test medical staff with email {} already exists.", medicalStaffEmail);
        }

        // 4. Создаем тестового пациента, если его еще нет (включая Patient профиль)
        String patientEmail = "patient.test@example.com";
        if (userRepository.findByEmail(patientEmail).isEmpty()) {
            log.info("DataInitializer: Creating test patient with email: {}", patientEmail);
            Role patientRole = roleRepository.findByName(UserRole.PATIENT)
                    .orElseThrow(() -> new RuntimeException("Error: PATIENT role not found."));

            UserEntity patientUserEntity = new UserEntity();
            patientUserEntity.setEmail(patientEmail);
            patientUserEntity.setFullName("Пациент Тестов Пациентович");
            patientUserEntity.setPassword(passwordEncoder.encode("password123"));
            patientUserEntity.setRole(patientRole);
            patientUserEntity.setActive(true);
            UserEntity savedPatientUser = userRepository.save(patientUserEntity);

            Patient patientProfile = new Patient();
            patientProfile.setUser(savedPatientUser);
            patientProfile.setPhoneNumber("1234567890");
            patientProfile.setAddress("Тестовый адрес пациента, д.1");
            patientRepository.save(patientProfile);
            log.info("DataInitializer: Test patient {} and their profile created successfully.", patientEmail);
        } else {
            log.info("DataInitializer: Test patient with email {} already exists.", patientEmail);
        }

        // 5. Создаем шаблоны протоколов, если их еще нет
        createProtocolTemplates();

        log.info("DataInitializer: Data initialization finished.");
    }

    private void createProtocolTemplates() {
        log.info("DataInitializer: Checking and creating protocol templates...");

        // Проверяем, есть ли уже шаблоны в базе
        long templatesCount = protocolTemplateRepository.count();
        if (templatesCount > 0) {
            log.info("DataInitializer: Protocol templates already exist ({}), skipping creation.", templatesCount);
            return;
        }

        // 1. Шаблон для ОРВИ
        ProtocolTemplate orviTemplate = new ProtocolTemplate();
        orviTemplate.setName("ОРВИ (острая респираторная вирусная инфекция)");
        orviTemplate.setDescription("Стандартный шаблон для диагностики и лечения ОРВИ");
        orviTemplate.setComplaintsTemplate("Повышение температуры тела, заложенность носа, насморк, кашель, общая слабость, головная боль");
        orviTemplate.setAnamnesisTemplate("Заболевание началось остро, 2-3 дня назад. Контакт с больными ОРВИ в семье/коллективе. Хронические заболевания отрицает. Аллергологический анамнез не отягощен.");
        orviTemplate.setObjectiveStatusTemplate("Общее состояние удовлетворительное. Кожные покровы обычной окраски. Слизистые носа гиперемированы, отечны. Зев гиперемирован. Пальпируются увеличенные подчелюстные лимфоузлы. Дыхание везикулярное, хрипов нет. Тоны сердца ясные, ритмичные. Живот мягкий, безболезненный.");
        orviTemplate.setRecommendationsTemplate("Режим домашний, обильное теплое питье, симптоматическая терапия. Парацетамол 500мг при температуре выше 38°C. Промывание носа солевыми растворами. При ухудшении состояния - обращение к врачу.");
        orviTemplate.setRequiredVitals(Arrays.asList("temperature", "pulse", "systolicBP", "diastolicBP"));

        // 2. Шаблон для артериальной гипертензии
        ProtocolTemplate hypertensionTemplate = new ProtocolTemplate();
        hypertensionTemplate.setName("Артериальная гипертензия");
        hypertensionTemplate.setDescription("Шаблон для контроля и коррекции артериального давления");
        hypertensionTemplate.setComplaintsTemplate("Головная боль, головокружение, мелькание \"мушек\" перед глазами, повышение артериального давления");
        hypertensionTemplate.setAnamnesisTemplate("Артериальная гипертензия в анамнезе в течение [указать период]. Регулярно принимает [указать препараты]. Наследственность отягощена по сердечно-сосудистым заболеваниям.");
        hypertensionTemplate.setObjectiveStatusTemplate("Общее состояние удовлетворительное. Кожные покровы обычной окраски. Отеков нет. Дыхание везикулярное. Тоны сердца приглушены, ритм правильный. Живот мягкий, безболезненный.");
        hypertensionTemplate.setRecommendationsTemplate("Контроль АД 2 раза в день. Диета с ограничением соли до 5г/сутки. Регулярный прием антигипертензивных препаратов. [Коррекция терапии при необходимости]. Контроль через 1-2 недели.");
        hypertensionTemplate.setRequiredVitals(Arrays.asList("temperature", "pulse", "systolicBP", "diastolicBP"));

        // 3. Шаблон для сахарного диабета
        ProtocolTemplate diabetesTemplate = new ProtocolTemplate();
        diabetesTemplate.setName("Сахарный диабет 2 типа");
        diabetesTemplate.setDescription("Шаблон для контроля сахарного диабета и коррекции терапии");
        diabetesTemplate.setComplaintsTemplate("Жажда, учащенное мочеиспускание, слабость, повышение уровня глюкозы крови");
        diabetesTemplate.setAnamnesisTemplate("Сахарный диабет 2 типа в течение [указать период]. Принимает [указать сахароснижающие препараты]. Диету соблюдает не всегда. Контроль гликемии нерегулярный.");
        diabetesTemplate.setObjectiveStatusTemplate("Общее состояние удовлетворительное. Кожные покровы сухие. Отеков нет. Дыхание везикулярное. Тоны сердца ясные, ритмичные. Живот мягкий, безболезненный. Стопы осмотрены - патологии не выявлено.");
        diabetesTemplate.setRecommendationsTemplate("Диета №9, регулярный контроль гликемии. Продолжить прием [указать препараты]. Контроль HbA1c через 3 месяца. Осмотр офтальмолога 1 раз в год. Наблюдение эндокринолога.");
        diabetesTemplate.setRequiredVitals(Arrays.asList("temperature", "pulse", "systolicBP", "diastolicBP", "bloodGlucose"));

        // 4. Шаблон для остеохондроза
        ProtocolTemplate osteochondrosisTemplate = new ProtocolTemplate();
        osteochondrosisTemplate.setName("Остеохондроз позвоночника");
        osteochondrosisTemplate.setDescription("Шаблон для диагностики и лечения дегенеративных изменений позвоночника");
        osteochondrosisTemplate.setComplaintsTemplate("Боли в спине/шее, ограничение подвижности, онемение в конечностях, головокружение");
        osteochondrosisTemplate.setAnamnesisTemplate("Боли беспокоят в течение [указать период]. Связывает с физической нагрузкой/переохлаждением. Ранее получал лечение [указать]. Профессиональные вредности [указать].");
        osteochondrosisTemplate.setObjectiveStatusTemplate("Общее состояние удовлетворительное. Вынужденное положение тела. Напряжение паравертебральных мышц. Болезненность при пальпации остистых отростков [указать уровень]. Симптомы натяжения [положительные/отрицательные].");
        osteochondrosisTemplate.setRecommendationsTemplate("Режим с ограничением физических нагрузок. НПВС [указать препарат и дозировку]. Местно - противовоспалительные мази. Физиотерапия после купирования острого периода. ЛФК.");
        osteochondrosisTemplate.setRequiredVitals(Arrays.asList("temperature", "pulse", "systolicBP", "diastolicBP"));

        // 5. Шаблон профилактического осмотра
        ProtocolTemplate checkupTemplate = new ProtocolTemplate();
        checkupTemplate.setName("Профилактический осмотр");
        checkupTemplate.setDescription("Шаблон для проведения планового медицинского осмотра");
        checkupTemplate.setComplaintsTemplate("Жалоб не предъявляет / Обратился для профилактического осмотра");
        checkupTemplate.setAnamnesisTemplate("Хронические заболевания [указать или отрицает]. Наследственность [указать]. Вредные привычки [указать]. Аллергологический анамнез [указать].");
        checkupTemplate.setObjectiveStatusTemplate("Общее состояние удовлетворительное. Кожные покровы и видимые слизистые обычной окраски, чистые. Лимфоузлы не увеличены. Дыхание везикулярное, хрипов нет. Тоны сердца ясные, ритмичные. Живот мягкий, безболезненный.");
        checkupTemplate.setRecommendationsTemplate("Общие рекомендации по здоровому образу жизни. Соблюдение режима труда и отдыха. Рациональное питание. Регулярные физические нагрузки. Профилактические осмотры согласно возрасту.");
        checkupTemplate.setRequiredVitals(Arrays.asList("temperature", "pulse", "systolicBP", "diastolicBP", "weight", "height"));

        // Сохраняем все шаблоны
        List<ProtocolTemplate> templates = Arrays.asList(
                orviTemplate,
                hypertensionTemplate,
                diabetesTemplate,
                osteochondrosisTemplate,
                checkupTemplate
        );

        for (ProtocolTemplate template : templates) {
            try {
                ProtocolTemplate saved = protocolTemplateRepository.save(template);
                log.info("DataInitializer: Created protocol template: {}", saved.getName());
            } catch (Exception e) {
                log.error("DataInitializer: Failed to create protocol template: {}", template.getName(), e);
            }
        }

        log.info("DataInitializer: Protocol templates creation completed. Total templates: {}",
                protocolTemplateRepository.count());
    }
}