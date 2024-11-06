package com.pollub.cookie.service;

import com.pollub.cookie.dto.DiscountCodeDTO;
import com.pollub.cookie.exception.ResourceNotFoundException;
import com.pollub.cookie.model.DiscountCode;
import com.pollub.cookie.model.DiscountType;
import com.pollub.cookie.repository.DiscountCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DiscountCodeService {

    private final DiscountCodeRepository discountCodeRepository;

    @Autowired
    public DiscountCodeService(DiscountCodeRepository discountCodeRepository) {
        this.discountCodeRepository = discountCodeRepository;
    }

    /**
     * Tworzy nowy kod rabatowy na podstawie DiscountCodeDTO.
     *
     * @param discountCodeDTO Dane kodu rabatowego
     * @return Utworzony kod rabatowy jako DiscountCodeDTO
     */
    @Transactional
    public DiscountCodeDTO createDiscountCode(DiscountCodeDTO discountCodeDTO) {

        if (discountCodeRepository.findByKod(discountCodeDTO.getKod()).isPresent()) {
            throw new IllegalArgumentException("Kod rabatowy o wartości '" + discountCodeDTO.getKod() + "' już istnieje.");
        }


        DiscountCode discountCode = mapToEntity(discountCodeDTO);


        DiscountCode savedDiscountCode = discountCodeRepository.save(discountCode);

        return mapToDTO(savedDiscountCode);
    }

    /**
     * Pobiera kod rabatowy po ID.
     *
     * @param id ID kodu rabatowego
     * @return Kod rabatowy jako DiscountCodeDTO
     */
    @Transactional(readOnly = true)
    public DiscountCodeDTO getDiscountCodeById(Long id) {
        DiscountCode discountCode = discountCodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kod rabatowy nie znaleziony o ID: " + id));
        return mapToDTO(discountCode);
    }

    /**
     * Pobiera wszystkie kody rabatowe.
     *
     * @return Lista kodów rabatowych jako DiscountCodeDTO
     */
    @Transactional(readOnly = true)
    public List<DiscountCodeDTO> getAllDiscountCodes() {
        List<DiscountCode> discountCodes = discountCodeRepository.findAll();
        return discountCodes.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Aktualizuje istniejący kod rabatowy.
     *
     * @param id               ID kodu rabatowego do aktualizacji
     * @param discountCodeDTO  Nowe dane kodu rabatowego
     * @return Zaktualizowany kod rabatowy jako DiscountCodeDTO
     */
    @Transactional
    public DiscountCodeDTO updateDiscountCode(Long id, DiscountCodeDTO discountCodeDTO) {
        DiscountCode existingDiscountCode = discountCodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kod rabatowy nie znaleziony o ID: " + id));

        if (!existingDiscountCode.getKod().equals(discountCodeDTO.getKod())) {

            if (discountCodeRepository.findByKod(discountCodeDTO.getKod()).isPresent()) {
                throw new IllegalArgumentException("Kod rabatowy o wartości '" + discountCodeDTO.getKod() + "' już istnieje.");
            }
            existingDiscountCode.setKod(discountCodeDTO.getKod());
        }

        existingDiscountCode.setTyp(mapTypStringToEnum(discountCodeDTO.getTyp()));

        existingDiscountCode.setWartosc(discountCodeDTO.getWartosc());

        existingDiscountCode.setDataWaznosci(discountCodeDTO.getDataWaznosci());

        DiscountCode updatedDiscountCode = discountCodeRepository.save(existingDiscountCode);

        return mapToDTO(updatedDiscountCode);
    }

    /**
     * Usuwa kod rabatowy po ID.
     *
     * @param id ID kodu rabatowego do usunięcia
     */
    @Transactional
    public void deleteDiscountCode(Long id) {
        if (!discountCodeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Kod rabatowy nie znaleziony o ID: " + id);
        }
        discountCodeRepository.deleteById(id);
    }


    /**
     * Mapuje DiscountCodeDTO na encję DiscountCode.
     *
     * @param discountCodeDTO Dane kodu rabatowego
     * @return Encja DiscountCode
     */
    private DiscountCode mapToEntity(DiscountCodeDTO discountCodeDTO) {
        DiscountCode discountCode = new DiscountCode();
        discountCode.setKod(discountCodeDTO.getKod());
        discountCode.setTyp(mapTypStringToEnum(discountCodeDTO.getTyp()));
        discountCode.setWartosc(discountCodeDTO.getWartosc());
        discountCode.setDataWaznosci(discountCodeDTO.getDataWaznosci());
        return discountCode;
    }

    /**
     * Mapuje encję DiscountCode na DiscountCodeDTO.
     *
     * @param discountCode Encja DiscountCode
     * @return DiscountCodeDTO
     */
    private DiscountCodeDTO mapToDTO(DiscountCode discountCode) {
        DiscountCodeDTO discountCodeDTO = new DiscountCodeDTO();
        discountCodeDTO.setId(discountCode.getId());
        discountCodeDTO.setKod(discountCode.getKod());
        discountCodeDTO.setTyp(discountCode.getTyp().name());
        discountCodeDTO.setWartosc(discountCode.getWartosc());
        discountCodeDTO.setDataWaznosci(discountCode.getDataWaznosci());
        return discountCodeDTO;
    }

    /**
     * Mapuje String typ rabatu na enum DiscountType.
     *
     * @param typString Typ rabatu jako String
     * @return Enum DiscountType
     */
    private DiscountType mapTypStringToEnum(String typString) {
        try {
            return DiscountType.valueOf(typString.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Nieprawidłowy typ rabatu: " + typString);
        }
    }

    @Transactional(readOnly = true)
    public DiscountCodeDTO getDiscountCodeByKod(String kod) {
        DiscountCode discountCode = discountCodeRepository.findByKod(kod)
                .orElseThrow(() -> new ResourceNotFoundException("Kod rabatowy nie znaleziony: " + kod));

        if (discountCode.getDataWaznosci().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Kod rabatowy wygasł.");
        }

        return mapToDTO(discountCode);
    }
}
