package github.drewlakee.telegram.commands;

import github.drewlakee.telegram.commands.callbacks.PostCallback;
import github.drewlakee.telegram.commands.handlers.CallbackQueryHandler;
import github.drewlakee.telegram.commands.handlers.MessageHandler;
import github.drewlakee.telegram.commands.keyboards.HostGroupKeyboard;
import github.drewlakee.telegram.commands.keyboards.InlineKeyboardBuilder;
import github.drewlakee.telegram.commands.keyboards.NumpadKeyboardBuilder;
import github.drewlakee.telegram.utils.MessageKeysParser;
import github.drewlakee.telegram.utils.ResponseMessageDispatcher;
import github.drewlakee.vk.domain.attachments.VkAttachment;
import github.drewlakee.vk.domain.attachments.VkAudioAttachment;
import github.drewlakee.vk.domain.attachments.VkPhotoAttachment;
import github.drewlakee.vk.domain.groups.VkGroupFullDecorator;
import github.drewlakee.vk.domain.groups.VkGroupsCustodian;
import github.drewlakee.vk.services.VkWallPostService;
import github.drewlakee.vk.services.random.VkRandomAudioContent;
import github.drewlakee.vk.services.random.VkRandomPhotoContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PostCommand extends BotCommand implements CallbackQueryHandler, MessageHandler {

    public final VkGroupsCustodian custodian;
    public final VkWallPostService vkWallPostService;
    public final VkRandomAudioContent randomAudioContent;
    public final VkRandomPhotoContent randomPhotoContent;

    public static final String COMMAND_NAME = "/post";
    public static final int MAX_VK_ATTACHMENTS = 10;

    @Autowired
    public PostCommand(VkGroupsCustodian custodian, VkWallPostService vkWallPostService,
                       VkRandomAudioContent randomAudioContent, VkRandomPhotoContent randomPhotoContent) {
        super(COMMAND_NAME);
        this.custodian = custodian;
        this.vkWallPostService = vkWallPostService;
        this.randomAudioContent = randomAudioContent;
        this.randomPhotoContent = randomPhotoContent;
    }

    @Override
    public void handle(AbsSender sender, Message message) {
        SendMessage response = new SendMessage();
        response.setChatId(message.getChatId());
        response.setText("Choose quantity of photos: ");
        NumpadKeyboardBuilder numpad = new NumpadKeyboardBuilder(4, MAX_VK_ATTACHMENTS);
        response.setReplyMarkup(numpad.build( COMMAND_NAME + "_first_call_photo", true));
        ResponseMessageDispatcher.send(sender, response);
    }

    @Override
    public void handle(AbsSender sender, CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        PostCallback handleCallback = PostCallback.CONSTRUCT_CALLBACK;
        int audiosQuantity = 0;
        int photosQuantity = 0;

        if (data.contains("photo_numpad")) {
            if (data.contains("_first_call_")) {
                photosQuantity = Integer.parseInt(data.replace(COMMAND_NAME + "_first_call_photo_numpad", ""));
                if (photosQuantity < 10) {
                    handleCallback = PostCallback.CHANGE_AUDIO_QUANTITY_CALLBACK;
                }
            } else {
                Map<String, String> keys = MessageKeysParser.parseMessageKeysBody(callbackQuery.getMessage().getText());
                photosQuantity = Integer.parseInt(data.replace(COMMAND_NAME + "photo_numpad", ""));
                audiosQuantity = Integer.parseInt(keys.get("audios_quantity"));
            }
        }

        if (data.contains("audio_numpad")) {
            Map<String, String> keys = MessageKeysParser.parseMessageKeysBody(callbackQuery.getMessage().getText());
            photosQuantity = Integer.parseInt(keys.get("photos_quantity"));
            audiosQuantity = Integer.parseInt(data.replace(COMMAND_NAME + "audio_numpad", ""));
        }

        if (data.contains(PostCallback.CHANGE_SET_CALLBACK.toCallbackString(COMMAND_NAME))) {
            Map<String, String> keys = MessageKeysParser.parseMessageKeysBody(callbackQuery.getMessage().getText());
            photosQuantity = Integer.parseInt(keys.get("photos_quantity"));
            audiosQuantity = Integer.parseInt(keys.get("audios_quantity"));
        }

        if (data.contains(PostCallback.CHANGE_AUDIO_QUANTITY_CALLBACK.toCallbackString(COMMAND_NAME))) {
            handleCallback = PostCallback.CHANGE_AUDIO_QUANTITY_CALLBACK;
            Map<String, String> keys = MessageKeysParser.parseMessageKeysBody(callbackQuery.getMessage().getText());
            photosQuantity = Integer.parseInt(keys.get("photos_quantity"));
            audiosQuantity = Integer.parseInt(keys.get("audios_quantity"));
        }

        if (data.contains(PostCallback.CHANGE_PHOTO_QUANTITY_CALLBACK.toCallbackString(COMMAND_NAME))) {
            handleCallback = PostCallback.CHANGE_PHOTO_QUANTITY_CALLBACK;
            Map<String, String> keys = MessageKeysParser.parseMessageKeysBody(callbackQuery.getMessage().getText());
            photosQuantity = Integer.parseInt(keys.get("photos_quantity"));
            audiosQuantity = Integer.parseInt(keys.get("audios_quantity"));
        }

        if (data.contains(PostCallback.SEND_CALLBACK.toCallbackString(COMMAND_NAME))) {
            handleCallback = PostCallback.SEND_CALLBACK;
        }

        int groupId = 0;
        if (data.contains(COMMAND_NAME + "_group_id")) {
            handleCallback = PostCallback.GROUP_CALLBACK;
            groupId = Integer.parseInt(data.replace(COMMAND_NAME + "_group_id", ""));
        }

        switch (handleCallback) {
            case CONSTRUCT_CALLBACK:
                sendContentSet(sender, callbackQuery, photosQuantity, audiosQuantity);
                break;
            case CHANGE_PHOTO_QUANTITY_CALLBACK:
                sendPhotoQuantityNumpad(sender, callbackQuery, audiosQuantity);
                break;
            case GROUP_CALLBACK:
                sendSetToGroup(sender, callbackQuery, groupId);
                break;
            case SEND_CALLBACK:
                sendGroupKeyboard(sender, callbackQuery);
                break;
            default:
                sendAudioQuantityNumpad(sender, callbackQuery, photosQuantity);
        }
    }

    private void sendContentSet(AbsSender sender, CallbackQuery callbackQuery, int photosQuantity, int audiosQuantity) {
        EditMessageText response = new EditMessageText();
        response.setChatId(callbackQuery.getMessage().getChatId());
        response.setMessageId(callbackQuery.getMessage().getMessageId());
        response.setParseMode(ParseMode.HTML);

        List<VkAttachment> attachments = new ArrayList<>();

        if (photosQuantity > 0) {
            attachments.addAll(randomPhotoContent.find(photosQuantity));
        }

        if (audiosQuantity > 0) {
            attachments.addAll(randomAudioContent.find(audiosQuantity));
        }

        response.setText(fillTextBody(attachments, photosQuantity, audiosQuantity));
        if (audiosQuantity > 0 || photosQuantity > 0) {
            response.setReplyMarkup(buildConstructKeyboard());
        }

        ResponseMessageDispatcher.send(sender, response);
    }

    private String fillTextBody(List<VkAttachment> attachments, int photosQuantity, int audiosQuantity) {
        StringBuilder text = new StringBuilder();
        if (audiosQuantity == 0 && photosQuantity == 0) {
            text.append("oopsy doopsy, nothing to look for...");
            return text.toString();
        } else {
            text.append("photos_quantity: ").append(photosQuantity).append("\n");
            text.append("audios_quantity: ").append(audiosQuantity).append("\n");
            text.append("\n");
        }

        if (photosQuantity > 0) {
            List<VkAttachment> photos = attachments.stream()
                    .filter(attach -> attach instanceof VkPhotoAttachment)
                    .collect(Collectors.toList());

            int count = 1;
            for (VkAttachment photo : photos) {
                VkPhotoAttachment vkCustomPhoto = (VkPhotoAttachment) photo;
                URL photoURL = vkCustomPhoto.getLargestSize().getUrl();
                String vkAttachmentFormat = photo.toAttachmentString();

                text.append("photo_").append(count).append(": ");
                text.append("<a href=\"").append(photoURL).append("\">").append(vkAttachmentFormat).append("</a>");
                text.append("\n");
                count++;
            }
        }

        if (photosQuantity > 0 && audiosQuantity > 0) {
            text.append("\n");
        }

        if (audiosQuantity > 0) {
            List<VkAttachment> audios = attachments.stream()
                    .filter(attach -> attach instanceof VkAudioAttachment)
                    .collect(Collectors.toList());

            int count = 1;
            for (VkAttachment audio : audios) {
                VkAudioAttachment vkAudioAttachment = (VkAudioAttachment) audio;
                String prettyNameOfAudio = vkAudioAttachment.toPrettyString();
                String vkAttachmentFormat = audio.toAttachmentString();

                text.append("audio_").append(count).append(": ");
                text.append(vkAttachmentFormat).append(" (").append(prettyNameOfAudio).append(")");
                text.append("\n");
                count++;
            }
        }

        return text.toString();
    }

    private void sendAudioQuantityNumpad(AbsSender sender, CallbackQuery callbackQuery, int photoQuantity) {
        EditMessageText response = new EditMessageText();
        response.setChatId(callbackQuery.getMessage().getChatId());
        response.setMessageId(callbackQuery.getMessage().getMessageId());
        response.setText("photos_quantity: " + photoQuantity + "\n\nChoose audios quantity: ");
        NumpadKeyboardBuilder numpad = new NumpadKeyboardBuilder(4, MAX_VK_ATTACHMENTS - photoQuantity);
        response.setReplyMarkup(numpad.build(COMMAND_NAME + "audio", true));
        ResponseMessageDispatcher.send(sender, response);
    }

    private void sendPhotoQuantityNumpad(AbsSender sender, CallbackQuery callbackQuery, int audioQuantity) {
        EditMessageText response = new EditMessageText();
        response.setChatId(callbackQuery.getMessage().getChatId());
        response.setMessageId(callbackQuery.getMessage().getMessageId());
        response.setText("audios_quantity: " + audioQuantity + "\n\nChoose photos quantity: ");
        NumpadKeyboardBuilder numpad = new NumpadKeyboardBuilder(4, MAX_VK_ATTACHMENTS - audioQuantity);
        response.setReplyMarkup(numpad.build(COMMAND_NAME + "photo", true));
        ResponseMessageDispatcher.send(sender, response);
    }

    private void sendSetToGroup(AbsSender sender, CallbackQuery callbackQuery, int groupId) {
        Map<String, String> keys = MessageKeysParser.parseMessageKeysBody(callbackQuery.getMessage().getText());
        List<String> audiosKeys = keys.keySet().stream().filter(key -> key.startsWith("audio_")).collect(Collectors.toList());
        List<String> photosKeys = keys.keySet().stream().filter(key -> key.startsWith("photo_")).collect(Collectors.toList());
        List<String> vkAttachments = new ArrayList<>();

        for (String key : audiosKeys) {
            vkAttachments.add(keys.get(key));
        }

        for (String key : photosKeys) {
            vkAttachments.add(keys.get(key));
        }

        Optional<VkGroupFullDecorator> first = custodian.getGroupsWithEditableRights().stream().filter(group -> group.getGroupFull().getId() == groupId).findFirst();

        EditMessageReplyMarkup response = new EditMessageReplyMarkup();
        response.setChatId(callbackQuery.getMessage().getChatId());
        response.setMessageId(callbackQuery.getMessage().getMessageId());
        InlineKeyboardBuilder keyboardBuilder = new InlineKeyboardBuilder();
        boolean isRequestSuccessful = vkWallPostService.makePost(first.orElseThrow(), vkAttachments);
        if (isRequestSuccessful) {
            keyboardBuilder.addButton(
                    new InlineKeyboardButton()
                            .setText("Post was send to " + first.orElseThrow().getGroupFull().getName() + " group!")
                            .setUrl("https://vk.com/" + first.orElseThrow().getGroupFull().getScreenName())
            );
        } else {
            keyboardBuilder.addButton(new InlineKeyboardButton()
                    .setText("Oops... something was broken on the way!")
                    .setUrl("https://vk.com/%20error"));
        }

        response.setReplyMarkup(keyboardBuilder.build());
        ResponseMessageDispatcher.send(sender, response);
    }

    private void sendGroupKeyboard(AbsSender sender, CallbackQuery callbackQuery) {
        EditMessageReplyMarkup response = new EditMessageReplyMarkup();
        response.setChatId(callbackQuery.getMessage().getChatId());
        response.setMessageId(callbackQuery.getMessage().getMessageId());
        response.setReplyMarkup(new HostGroupKeyboard(custodian).build(COMMAND_NAME, true));
        ResponseMessageDispatcher.send(sender, response);
    }

    private InlineKeyboardMarkup buildConstructKeyboard() {
        return new InlineKeyboardBuilder()
                .addButton(new InlineKeyboardButton()
                    .setText("Change content set")
                    .setCallbackData(PostCallback.CHANGE_SET_CALLBACK.toCallbackString(COMMAND_NAME)))
                .nextLine()
                .addButton(new InlineKeyboardButton()
                        .setText("Change audio quantity")
                        .setCallbackData(PostCallback.CHANGE_AUDIO_QUANTITY_CALLBACK.toCallbackString(COMMAND_NAME)))
                .addButton(new InlineKeyboardButton()
                        .setText("Change photo quantity")
                        .setCallbackData(PostCallback.CHANGE_PHOTO_QUANTITY_CALLBACK.toCallbackString(COMMAND_NAME)))
                .nextLine()
                .addButton(new InlineKeyboardButton()
                        .setText("Send")
                        .setCallbackData(PostCallback.SEND_CALLBACK.toCallbackString(COMMAND_NAME)))
                .nextLine()
                .addButton(new InlineKeyboardButton()
                        .setText("Cancel")
                        .setCallbackData("delete_message"))
                .build();
    }
}
