package com.busapi.modules.identity.service;

import com.busapi.core.exception.BusinessException;
import com.busapi.core.exception.ResourceNotFoundException;
import com.busapi.modules.identity.dto.CreateUserRequest;
import com.busapi.modules.identity.dto.UserHistoryResponse;
import com.busapi.modules.identity.dto.UserResponse;
import com.busapi.modules.identity.entity.Agency;
import com.busapi.modules.identity.entity.User;
import com.busapi.modules.identity.mapper.UserMapper;
import com.busapi.modules.identity.repository.AgencyRepository;
import com.busapi.modules.identity.repository.UserRepository;
import com.busapi.modules.sales.dto.TicketResponse;
import com.busapi.modules.sales.entity.Ticket;
import com.busapi.modules.sales.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final AgencyRepository agencyRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final TicketRepository ticketRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Kullanıcı bulunamadı: " + username));
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        // 1. Email kontrolü
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Bu e-posta adresi zaten kullanımda.");
        }

        // 2. Entity dönüşümü
        User user = userMapper.toEntity(request);

        // 3. Şifre Hashleme
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // 4. Acenta Kontrolü (Eğer personel ise)
        if (request.getAgencyId() != null) {
            Agency agency = agencyRepository.findById(request.getAgencyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Agency", "id", request.getAgencyId()));
            user.setAgency(agency);
        }

        // 5. Kayıt
        User savedUser = userRepository.save(user);

        return userMapper.toResponse(savedUser);
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return userMapper.toResponse(user);
    }

    public User findUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    // Auth modülü için gerekli (Login sırasında kullanılacak)
    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    public void updateUser(User user) {
        userRepository.save(user);
    }

    public UserHistoryResponse getUserHistory(Long userId) {
        User user = findUserEntityById(userId);

        // Repository'e bu metodu eklemelisin: findByUserIdOrderByTripDateDesc
        List<Ticket> tickets = ticketRepository.findByUserId(userId);

        // Ticket -> TicketResponse çevrimi (Mapper kullanılabilir veya manuel)
        List<TicketResponse> ticketResponses = tickets.stream()
                .map(t -> {
                    TicketResponse r = new TicketResponse();
                    r.setPnrCode(t.getOrder().getOrderPnr()); // Order'dan alıyoruz
                    r.setTripDescription(t.getTrip().getVoyage().getRoute().getName());
                    r.setPrice(t.getPrice());
                    // ... diğer alanlar
                    return r;
                }).toList();

        return UserHistoryResponse.builder()
                .currentPoints(user.getCurrentPoints())
                .pastTickets(ticketResponses)
                .build();
    }
}
