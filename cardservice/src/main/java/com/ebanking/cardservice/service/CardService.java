package com.ebanking.cardservice.service;

import com.ebanking.cardservice.model.Card;
import com.ebanking.cardservice.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CardService {
    
    private final CardRepository cardRepository;
    
    public List<Card> getAllCards() {
        return cardRepository.findAll();
    }
    
    public Optional<Card> getCardById(Long id) {
        return cardRepository.findById(id);
    }
    
    public List<Card> getCardsByAccountId(Long accountId) {
        return cardRepository.findByAccountId(accountId);
    }
    
    public Card activateCard(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card not found"));
        card.setStatus(Card.CardStatus.ACTIVE);
        return cardRepository.save(card);
    }
    
    public Card deactivateCard(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card not found"));
        card.setStatus(Card.CardStatus.INACTIVE);
        return cardRepository.save(card);
    }
    
    public Card blockCard(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card not found"));
        card.setStatus(Card.CardStatus.BLOCKED);
        return cardRepository.save(card);
    }
    
    public Card createCard(Card card) {
        return cardRepository.save(card);
    }
    
    public Card updateCard(Long id, Card card) {
        Card existingCard = cardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card not found"));
        
        existingCard.setCardNumber(card.getCardNumber());
        existingCard.setCardType(card.getCardType());
        existingCard.setStatus(card.getStatus());
        existingCard.setUserId(card.getUserId());
        existingCard.setAccountId(card.getAccountId());
        
        return cardRepository.save(existingCard);
    }
    
    public void deleteCard(Long id) {
        cardRepository.deleteById(id);
    }
}
