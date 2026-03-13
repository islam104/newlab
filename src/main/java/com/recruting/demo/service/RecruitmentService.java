package com.recruting.demo.service;

import com.recruting.demo.dto.*;
import com.recruting.demo.entity.*;
import com.recruting.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RecruitmentService {
    
    private final VacancyRepository vacancyRepository;
    private final CandidateRepository candidateRepository;
    private final ApplicationRepository applicationRepository;
    private final InterviewRepository interviewRepository;
    private final OfferRepository offerRepository;
    
    // Бизнес-операция 1: Подача заявки на вакансию
    public ApplicationDTO applyForVacancy(Long vacancyId, Long candidateId, String coverLetter) {
        Vacancy vacancy = vacancyRepository.findById(vacancyId)
                .orElseThrow(() -> new RuntimeException("Вакансия не найдена"));
        
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new RuntimeException("Кандидат не найден"));
        
        if (applicationRepository.findByVacancyIdAndCandidateId(vacancyId, candidateId).isPresent()) {
            throw new RuntimeException("Вы уже подавали заявку на эту вакансию");
        }
        
        if (vacancy.getStatus() != Vacancy.VacancyStatus.ACTIVE) {
            throw new RuntimeException("Вакансия неактивна");
        }
        
        Application application = new Application();
        application.setVacancy(vacancy);
        application.setCandidate(candidate);
        application.setCoverLetter(coverLetter);
        application.setStatus(Application.ApplicationStatus.SUBMITTED);
        
        Application savedApplication = applicationRepository.save(application);
        return convertToApplicationDTO(savedApplication);
    }
    
    // Бизнес-операция 2: Планирование интервью с проверкой конфликтов
    public InterviewDTO scheduleInterview(Long applicationId, String interviewer, 
                                        LocalDateTime scheduledTime, Integer durationMinutes) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Заявка не найдена"));
        
        if (application.getStatus() != Application.ApplicationStatus.UNDER_REVIEW) {
            throw new RuntimeException("Нельзя запланировать интервью для этой заявки");
        }
        
        LocalDateTime endTime = scheduledTime.plusMinutes(durationMinutes != null ? durationMinutes : 60);
        
        List<Interview> conflictingInterviews = interviewRepository.findConflictingInterviews(
                interviewer, scheduledTime, endTime);
        
        if (!conflictingInterviews.isEmpty()) {
            throw new RuntimeException("У интервьюера уже есть запланированное интервью на это время");
        }
        
        Interview interview = new Interview();
        interview.setApplication(application);
        interview.setInterviewer(interviewer);
        interview.setScheduledTime(scheduledTime);
        interview.setDurationMinutes(durationMinutes != null ? durationMinutes : 60);
        interview.setStatus(Interview.InterviewStatus.SCHEDULED);
        interview.setType(Interview.InterviewType.TECHNICAL);
        
        Interview savedInterview = interviewRepository.save(interview);
        
        application.setStatus(Application.ApplicationStatus.INTERVIEW_SCHEDULED);
        applicationRepository.save(application);
        
        return convertToInterviewDTO(savedInterview);
    }
    
    // Бизнес-операция 3: Создание оффера с проверкой уникальности
    public OfferDTO createOffer(Long applicationId, java.math.BigDecimal salary, 
                               java.time.LocalDate startDate, String terms) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Заявка не найдена"));
        
        Candidate candidate = application.getCandidate();
        
        if (offerRepository.existsByCandidateIdAndStatus(candidate.getId(), Offer.OfferStatus.ACCEPTED)) {
            throw new RuntimeException("Кандидат уже принял другой оффер");
        }
        
        if (offerRepository.existsByCandidateIdAndStatus(candidate.getId(), Offer.OfferStatus.PENDING)) {
            throw new RuntimeException("Кандидату уже отправлен оффер, который ожидает рассмотрения");
        }
        
        if (application.getStatus() != Application.ApplicationStatus.INTERVIEW_COMPLETED) {
            throw new RuntimeException("Нельзя создать оффер для заявки, которая не прошла интервью");
        }
        
        Offer offer = new Offer();
        offer.setApplication(application);
        offer.setCandidate(candidate);
        offer.setSalary(salary);
        offer.setStartDate(startDate);
        offer.setOfferExpirationDate(java.time.LocalDate.now().plusDays(7));
        offer.setTerms(terms);
        offer.setStatus(Offer.OfferStatus.PENDING);
        
        Offer savedOffer = offerRepository.save(offer);
        
        application.setStatus(Application.ApplicationStatus.ACCEPTED);
        applicationRepository.save(application);
        
        return convertToOfferDTO(savedOffer);
    }
    
    // Бизнес-операция 4: Принятие оффера (кандидат может принять только один)
    public OfferDTO acceptOffer(Long offerId) {
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Оффер не найден"));
        
        if (offer.getStatus() != Offer.OfferStatus.PENDING) {
            throw new RuntimeException("Оффер не может быть принят");
        }
        
        if (offer.getOfferExpirationDate().isBefore(java.time.LocalDate.now())) {
            throw new RuntimeException("Срок действия оффера истек");
        }
        
        Candidate candidate = offer.getCandidate();
        
        List<Offer> otherPendingOffers = offerRepository.findByCandidateId(candidate.getId())
                .stream()
                .filter(o -> o.getId() != offerId && o.getStatus() == Offer.OfferStatus.PENDING)
                .collect(Collectors.toList());
        
        for (Offer otherOffer : otherPendingOffers) {
            otherOffer.setStatus(Offer.OfferStatus.REJECTED);
            offerRepository.save(otherOffer);
        }
        
        offer.setStatus(Offer.OfferStatus.ACCEPTED);
        Offer savedOffer = offerRepository.save(offer);
        
        Application application = offer.getApplication();
        application.setStatus(Application.ApplicationStatus.ACCEPTED);
        applicationRepository.save(application);
        
        return convertToOfferDTO(savedOffer);
    }
    
    // Бизнес-операция 5: Получение статистики по вакансии
    public VacancyStatisticsDTO getVacancyStatistics(Long vacancyId) {
        Vacancy vacancy = vacancyRepository.findById(vacancyId)
                .orElseThrow(() -> new RuntimeException("Вакансия не найдена"));
        
        List<Application> applications = applicationRepository.findByVacancyId(vacancyId);
        
        long totalApplications = applications.size();
        long submittedCount = applications.stream()
                .filter(a -> a.getStatus() == Application.ApplicationStatus.SUBMITTED)
                .count();
        long underReviewCount = applications.stream()
                .filter(a -> a.getStatus() == Application.ApplicationStatus.UNDER_REVIEW)
                .count();
        long interviewScheduledCount = applications.stream()
                .filter(a -> a.getStatus() == Application.ApplicationStatus.INTERVIEW_SCHEDULED)
                .count();
        long interviewCompletedCount = applications.stream()
                .filter(a -> a.getStatus() == Application.ApplicationStatus.INTERVIEW_COMPLETED)
                .count();
        long rejectedCount = applications.stream()
                .filter(a -> a.getStatus() == Application.ApplicationStatus.REJECTED)
                .count();
        long acceptedCount = applications.stream()
                .filter(a -> a.getStatus() == Application.ApplicationStatus.ACCEPTED)
                .count();
        
        List<Offer> offers = applications.stream()
                .flatMap(app -> app.getOffer() != null ? 
                        java.util.stream.Stream.of(app.getOffer()) : java.util.stream.Stream.empty())
                .collect(Collectors.toList());
        
        long totalOffers = offers.size();
        long pendingOffers = offers.stream()
                .filter(o -> o.getStatus() == Offer.OfferStatus.PENDING)
                .count();
        long acceptedOffers = offers.stream()
                .filter(o -> o.getStatus() == Offer.OfferStatus.ACCEPTED)
                .count();
        long rejectedOffers = offers.stream()
                .filter(o -> o.getStatus() == Offer.OfferStatus.REJECTED)
                .count();
        
        VacancyStatisticsDTO stats = new VacancyStatisticsDTO();
        stats.setVacancyId(vacancyId);
        stats.setVacancyTitle(vacancy.getTitle());
        stats.setTotalApplications(totalApplications);
        stats.setSubmittedCount(submittedCount);
        stats.setUnderReviewCount(underReviewCount);
        stats.setInterviewScheduledCount(interviewScheduledCount);
        stats.setInterviewCompletedCount(interviewCompletedCount);
        stats.setRejectedCount(rejectedCount);
        stats.setAcceptedCount(acceptedCount);
        stats.setTotalOffers(totalOffers);
        stats.setPendingOffers(pendingOffers);
        stats.setAcceptedOffers(acceptedOffers);
        stats.setRejectedOffers(rejectedOffers);
        
        return stats;
    }
    
    private ApplicationDTO convertToApplicationDTO(Application application) {
        ApplicationDTO dto = new ApplicationDTO();
        dto.setId(application.getId());
        dto.setVacancyId(application.getVacancy().getId());
        dto.setCandidateId(application.getCandidate().getId());
        dto.setCoverLetter(application.getCoverLetter());
        dto.setStatus(application.getStatus().toString());
        dto.setVacancyTitle(application.getVacancy().getTitle());
        dto.setCandidateName(application.getCandidate().getFirstName() + " " + 
                            application.getCandidate().getLastName());
        dto.setCandidateEmail(application.getCandidate().getEmail());
        return dto;
    }
    
    private InterviewDTO convertToInterviewDTO(Interview interview) {
        InterviewDTO dto = new InterviewDTO();
        dto.setId(interview.getId());
        dto.setApplicationId(interview.getApplication().getId());
        dto.setInterviewer(interview.getInterviewer());
        dto.setScheduledTime(interview.getScheduledTime());
        dto.setDurationMinutes(interview.getDurationMinutes());
        dto.setStatus(interview.getStatus().toString());
        dto.setType(interview.getType().toString());
        dto.setNotes(interview.getNotes());
        dto.setCandidateName(interview.getApplication().getCandidate().getFirstName() + " " + 
                            interview.getApplication().getCandidate().getLastName());
        dto.setVacancyTitle(interview.getApplication().getVacancy().getTitle());
        return dto;
    }
    
    private OfferDTO convertToOfferDTO(Offer offer) {
        OfferDTO dto = new OfferDTO();
        dto.setId(offer.getId());
        dto.setApplicationId(offer.getApplication().getId());
        dto.setCandidateId(offer.getCandidate().getId());
        dto.setSalary(offer.getSalary());
        dto.setStartDate(offer.getStartDate());
        dto.setOfferExpirationDate(offer.getOfferExpirationDate());
        dto.setTerms(offer.getTerms());
        dto.setStatus(offer.getStatus().toString());
        dto.setCandidateName(offer.getCandidate().getFirstName() + " " + 
                            offer.getCandidate().getLastName());
        dto.setCandidateEmail(offer.getCandidate().getEmail());
        dto.setVacancyTitle(offer.getApplication().getVacancy().getTitle());
        return dto;
    }
}
